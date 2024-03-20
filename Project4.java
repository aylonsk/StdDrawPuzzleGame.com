import java.io.IOException;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.Random;

/**
 * Simulates a slider puzzle using supplied sub-folder of images and StdDraw class.
 */
public class Project4 {

   /**
    * Set up the puzzle and run the game loop until puzzle is solved.
    * @param args  command-line arguments, ignored
    * [PROVIDED STARTER CODE: DO NOT MODIFY EXCEPT FOR TEMPORARY
    *  CHANGE TO VALUE OF NUMSCRAMBLESTEPS WHILE TESTING]
    */
   public static void main(String[] args) throws IOException {
   
      //Prompt user for name of sub-folder where tiled image files are
      final String FOLDER = collectFolderName();
      
      //Obtain a populated array with names of tile images in
      //the arrangement that forms the completed puzzle image
      String[][] files = createImageArray(FOLDER);
      final int NUMROWS = files.length;
      final int NUMCOLS = files[0].length; 
   
      //Number of random swaps to perform at start to scramble target image
      final int NUMSCRAMBLESTEPS = 7;  //use small value here for testing!
                                                    
      //Set constants to represent sizes of tiles and canvas
      final int TILEHEIGHT = 150; //Height of one tile
      final int TILEWIDTH = 150; //Width of one tile
      final int SEP = 2; //Width of separator line between tiles
   
      //Constants calculated from values above, named only for ease of use
      //Full canvas dimensions take into account the tiles and separators.
      final int CANVASWIDTH = NUMCOLS * TILEWIDTH + 2 * NUMCOLS * SEP;  
      final int CANVASHEIGHT = NUMROWS * TILEHEIGHT + 2 * NUMROWS * SEP;
   
      //Set up canvas dimensions and drawing font and colors
      initializeCanvas(CANVASHEIGHT, CANVASWIDTH);
   
      //Briefly display starting grid of unscrambled image tiles (the goal)
      displayFilesInGrid(files, TILEHEIGHT, TILEWIDTH, SEP);
      StdDraw.pause(750);
      
      //Clear canvas and show message that tile scrambling is occurring
      StdDraw.clear(StdDraw.DARK_GRAY); 
      displayStatusMessage(CANVASWIDTH, CANVASHEIGHT, TILEHEIGHT, 
         "Scrambling...");
      
      //Scramble target image by executing specified number of scrambles
      //selected at random. Receive a length-2 array indicating resulting
      //location of blank space, with position 0 holding the row number in the
      //grid, and position 1 holding the column number
      int[] blankLoc = scrambleBoard(files, NUMSCRAMBLESTEPS);
   
      //Redisplay the (now scrambled) board
      displayFilesInGrid(files, TILEHEIGHT, TILEWIDTH, SEP);
   
      //Now allow user to play by clicking mouse to move blank space tile
      //either horizontally or vertically in grid
      double xCord;
      double yCord;

      int moveCount = 0; //counts total number of legal moves made by user
      
      //Loop through user moves repeatedly until puzzle is completed
      while (!puzzleSolved(files, blankLoc, FOLDER)) {
      
         //Collect and process one user move
         StdDraw.pause(1);
         if (StdDraw.isMousePressed()) {
         
            //Determine user's desired move based on mouse click location
            double clickX = StdDraw.mouseX();
            double clickY = StdDraw.mouseY();
            int newRow = NUMROWS - 1 - ((int) clickY / (TILEHEIGHT + 2 * SEP));
            int newCol = ((int) clickX / (TILEWIDTH + 2 * SEP));
         
            //Investigate whether click represents a legal move
                     
            if (movePatternIsValid(blankLoc, newRow, newCol)) {
               //pattern represents a valid move, and we know the
               //is on an actual board location already, so make
               //the move and update move counter
               makeMove(files, blankLoc, newRow, newCol);
               moveCount++;
               
            } else { //this is an invalid move - don't process or count it!
               displayStatusMessage(CANVASWIDTH, CANVASHEIGHT, TILEHEIGHT, 
                                    "Invalid Move. Try again!");
            }
            
            //Whether valid or not, redisplay board, then pause briefly
            displayFilesInGrid(files, TILEHEIGHT, TILEWIDTH, SEP);
            StdDraw.pause(500);
         
         }         
      }
      
      //Report that puzzle was solved
      displayEndOfGame(files, moveCount, CANVASWIDTH, CANVASHEIGHT);
   }
      

   /** 
    * Prompt user for name of sub-folder containing image files, and return
    * the sub-folder name with a '/' appended to the end. The only required
    * use of a Scanner object in the program occurs within this method.
    * @return the name of the images sub-folder with '/' appended to it
    */
   public static String collectFolderName() {
   
      //Display a prompt for the user (do not modify contents of this prompt)
      System.out.println("Enter name of puzzle sub-folder.");
      System.out.print("Colors (Easy), GrayTee (Medium), or Beach (Hard): ");
    
      Scanner scnr = new Scanner(System.in);
      String puzzleName = scnr.nextLine();
      if (puzzleName.equals("Colors") || puzzleName.equals("GrayTee") || puzzleName.equals("Beach")) {
         return puzzleName + "/";
      }
      else {
         System.out.println("ERROR: Not a valid puzzle sub-folder!");
         return collectFolderName();
      }
   }
   
   
   /**
    * Look in specified folder for puzzle dimensions, then create and return
    * an appropriately-sized grid populated with filenames arranged to
    * create the completed image. The specified folder name will end with "/",
    * and the folder will contain a file named dimensions.txt that specifies 
    * the dimensions of the desired grid. The number of rows in the puzzle grid
    * will be listed first in the file, followed by the number of columns.
    * The filenames are loaded into the result array row by row starting with, 
    * row 0, and proceeding from left to right with each column within a row, 
    * including the supplied path to an appropriate folder. File names are
    * the consecutive integers starting with 0, and each one ends with the
    * suffix .png. For example, 0.png(the checkerboard), 1.png, 2.png, etc.
    * @param folder the relative name of the folder containing the images
    * @return the array containing filenames
    * @throws IOException if dimensions.txt is not found in given folder
    */ 
  
   public static String[][] createImageArray(String folder) throws IOException {
     
   
      int rows;
      int columns;
      FileInputStream filestream = new FileInputStream(folder
         + "/dimensions.txt");
      Scanner inFS = new Scanner(filestream);
      rows = inFS.nextInt();
      columns = inFS.nextInt();
      
      String[][] puzzleArray = new String[rows][columns];
      
      int k = 0;
      for (int i = 0; i < rows; ++i) {
         for (int j = 0; j < columns; ++j) {
            puzzleArray[i][j] = folder + k + ".png";
            ++k;
         }
      }
      return puzzleArray;
   }


   /**
    * Display puzzle grid images that are the contents of the files array,
    * using the provided subroutine displayOneTile to help.
    * @param files  the two-dimensional array which holds the names of the
    *              image files representing the tiles
    * @param tileHeight  the height of a single tile in the puzzle
    * @param tileWidth  the width of a single tile in the puzzle
    * @param sep  the amount of space separating adjacent tiles
    */
   public static void displayFilesInGrid(String[][] files, int tileHeight, 
                                         int tileWidth, int sep) {
                                          
      int numColumns = files[0].length;
      int numRows = files.length;
      
      for (int i = 0; i < numRows; i++) {
         for (int j = 0; j < numColumns; j++) {
         
            displayOneTile(files[i][j], i, j, tileWidth, 
               tileHeight, sep, numRows);
         }
      }
   }

   /** 
    * Scramble the image filename locations within the grid by making
    * the specified number of randomly-selected moves to change the
    * location of the blank tile. This method assumes the starting 
    * location of the blank in the files grid is row 0, column 0.
    * @param files  the two-dimensional array which holds the names of the
    *               image files representing the tiles
    * @param numScrambleSteps  the number of moves to make in 
    *                          the scrambling process
    * @return  a new length-2 array holding the current location of 
    *          the blank tile, with position 0 holding the row number
    *          in the grid, and position 1 holding the column number
    */
   public static int[] scrambleBoard(String[][] files, 
                                     int numScrambleSteps) {
      int numMoves = 4;
      Random rand = new Random();
      int i = 0;
      int xCoor = 0;
      int yCoor = 0;
      
      int randomMove;
      String substitute;
      
      while (i < numScrambleSteps) {
         randomMove = rand.nextInt(numMoves);
         switch (randomMove + 1) {
            case 1:
               if (yCoor + 1 > files.length - 1) {
                  break;
               }
               else {
                  substitute = files[yCoor][xCoor];
                  files[yCoor][xCoor] = files[yCoor + 1][xCoor];
                  files[yCoor + 1][xCoor] = substitute;
                  yCoor++;
                  i++;
               }
               break;
            case 2:
               if (xCoor + 1 > files[0].length - 1) {
                  break;
               }
               else {
                  substitute = files[yCoor][xCoor];
                  files[yCoor][xCoor] = files[yCoor][xCoor + 1];
                  files[yCoor][xCoor + 1] = substitute;
                  xCoor++;
                  i++;
               }
            case 3: 
               if (yCoor - 1 <= 0) {
                  break;
               }
               else {
                  substitute = files[yCoor][xCoor];
                  files[yCoor][xCoor] = files[yCoor - 1][xCoor];
                  files[yCoor - 1][xCoor] = substitute;
                  yCoor--;
                  i++;
               }
            case 4:
               if (xCoor - 1 <= 0) {
                  break;
               }
               else {
                  substitute = files[yCoor][xCoor];
                  files[yCoor][xCoor] = files[yCoor][xCoor - 1];
                  files[yCoor][xCoor - 1] = substitute;
                  xCoor--;
                  i++;
               }
            default:
               break;
         }
      }
      
      int[] returnArray = new int[2];
      returnArray[0] = yCoor;
      returnArray[1] = xCoor;
      
      return returnArray;
   
   }

   /**
    * Determine if a row number and column number specify a valid move location
    * from the given row and column number representing the current location of
    * the blank space. Specifically, this checks if the sum of the magnitudes
    * of the differences in the row numbers and column numbers is exactly one.
    * Note: this method does not check whether the destination location
    * actually appears on the grid.    
    * @param blankLoc the array holding the current location of the blank tile
    *                 with position 0 holding the row number in the grid, and
    *                  position 1 holding the column number
    * @param destRow  the row number of the destination location
    * @param destCol  the column number of the destination location
    * @return  true if the move pattern is valid, and false otherwise
    */
   public static boolean movePatternIsValid(int[] blankLoc,
                                            int destRow, int destCol) {
      int yDifference = Math.abs(blankLoc[0] - destRow);
      int xDifference = Math.abs(blankLoc[1] - destCol);
      
      return yDifference + xDifference == 1;
   
   }
       
 

   /** 
    * Move blank square within files array to the destination indicated.
    * Assumes specified destination is legal for the given location of the
    * blank space, as specified in the blankLoc array, where the value in
    * position 0 denotes row number, and the value in position 1 denotes the
    * column number. This method should not be called until the destination
    * has been validated. Note that this method updates the values in
    * blankLoc to represent the move made.
    * @param files  the pre-instantiated two-dimensional array which holds the
    *               names of image files
    * @param blankLoc the array holding the current location of the blank tile
    *                 with position 0 holding the row number in the grid, and
    *                 position 1 holding the column number
    * @param destRow  the row of the destination for the blank
    * @param destCol  the column of the destination for the blank
    */
   public static void makeMove(String[][] files, int[] blankLoc,
                               int destRow, int destCol) {
   
      String substitute = files[blankLoc[0]][blankLoc[1]];
      files[blankLoc[0]][blankLoc[1]] = files[destRow][destCol];
      files[destRow][destCol] = substitute;
   
      blankLoc[0] = destRow;
      blankLoc[1] = destCol;
      
   }



  /** 
    * Determine whether puzzle grid matches desired target configuration.
    *  @param files  the two-dimensional array which holds the names of the
    *                 image files representing the tiles
    *  @param blankLoc the array holding the current location of the blank tile
    *                   with position 0 holding the row number in the grid, and
    *                   position 1 holding the column number
    *  @param folder  the name of the path to the image files used,
    *                 which includes the terminating slash character
    *  @return  true if the puzzle matches the target, false otherwise
    */
   public static boolean puzzleSolved(String[][] files, 
                                      int[] blankLoc,
                                      String folder) throws IOException {
   
      String[][] folder1;
      
      folder1 = createImageArray(folder);
      
      for (int i = 0; i < files.length; i++) {
         for (int j = 0; j < files[0].length; j++) {
            if (! folder1[i][j].equals(files[i][j])) {
               return false;
            }
         }
      }
            
      return true;
      
   }


   /** 
    * Initialize the drawing canvas size and drawing settings. 
    * @param canvasHeight the desired height of the drawing window
    * @param canvasWidth the desired width of the drawing window
    */     
   public static void initializeCanvas(int canvasHeight, int canvasWidth) {   
   
      //Initialize canvas
      StdDraw.setCanvasSize(canvasWidth, canvasHeight);
      StdDraw.setXscale(0, canvasWidth);
      StdDraw.setYscale(0, canvasHeight);
      
      //Set font for displayed text, and initial drawing color
      StdDraw.setFont(new java.awt.Font("SANS_SERIF", java.awt.Font.BOLD, 22));
      StdDraw.setPenColor(StdDraw.WHITE);
   }


   /**
    * Display a single tile in the two-dimensional grid.
    * @param filename  name of the image to display, including folder
    *  @param row  the image's row number in the grid
    * @param col  the image's column number in the grid
    * @param tileWidth  the width of a single tile in this display
    * @param tileHeight  the height of a single tile in this display
    * @param sep  the width of a separator line in this display
    * @param numRows  the total number of rows in this grid
    */
   public static void displayOneTile(String filename, int row, int col,  
                                     int tileWidth, int tileHeight, 
                                     int sep, int numRows) {   
      //StdDraw picture method takes the following five arguments:
      //  xLocCenter, yLocCenter, fileName, scaledWidth, scaledHeight);         
      StdDraw.picture(
         ((2 * col + 1) * sep) + (col + 0.5) * tileWidth, //how far right
         ((2 * (numRows - row) - 1) * sep) 
            + (numRows - row - 0.5) * tileHeight, //how far up
         filename, tileWidth, tileHeight);
   }

   /**
    * Display the specified status message at the bottom of the canvas, then
    * pause briefly. The message is shown in white text overlaid on a dark
    * grey rectangle so that it is visible even if canvas background is not
    * solid or not dark.
    * @param  canvasWidth  the width of the entire canvas
    * @param  canvasHeight  the height of the entire canvas
    * @param  tileHeight  the height of one tile in the grid
    * @param  message  the status message to display
    */
   public static void displayStatusMessage(int canvasWidth,
                                           int canvasHeight, 
                                           int tileHeight, 
                                           String message) {
      //provide a dark background for status message
      StdDraw.setPenColor(StdDraw.DARK_GRAY);
      StdDraw.filledRectangle(canvasWidth / 2.0, canvasHeight / 2.0,
                              canvasWidth / 2.0, tileHeight / 3.0);
                                 
      //display status message in light color      
      StdDraw.setPenColor(StdDraw.WHITE);
      StdDraw.text(canvasWidth / 2.0, canvasHeight / 2.0, message);
      
      //pause briefly
      StdDraw.pause(750);
   }



   /**
    * Display completed board with end-of-game message.
    * @param files  the two-dimensional array which holds the names of the
    *               image files representing the tiles
    * @param moveCount  the total number of actual moves made by user
    * @param canvasWidth  the width of the canvas in total
    * @param canvasHeight  the height of the canvas in total
    */
   public static void displayEndOfGame(String[][] files, int moveCount,
                                       int canvasWidth, int canvasHeight) {
   
      //Construct final message that reports number of actual moves,
      //taking care to use singular or plural as appropriate
      String message;
      if (moveCount == 1) {
         message = "Puzzle solved in 1 move!";
      } else {
         message = "Puzzle solved in " + moveCount + " moves!";
      }
   
      //Create a dark background covering most of the canvas before
      //displaying the constructed message in light color
      StdDraw.setPenColor(StdDraw.DARK_GRAY);
      StdDraw.filledRectangle(canvasWidth / 2.0, canvasHeight / 2.0,
                              canvasWidth * .45, canvasHeight * .45);    
      StdDraw.setPenColor(StdDraw.WHITE);
      StdDraw.text(canvasWidth / 2.0, canvasHeight / 2.0, message, 45);
   } 


}