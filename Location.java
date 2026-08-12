package GameObjects;

public class Location {
    private String letterNotation;
    private ChessBoard board;
    private int row;
    private int col;

    public Location(int row, int col, ChessBoard board) {
        this.board = board;

        this.row = row;
        this.col = col;

        this.letterNotation = convertLocation(this.row, this.col);
    }

    public Location(String letterNotation, ChessBoard board) {
        this.board = board;

        this.letterNotation = letterNotation;

        int[] position = convertLocation(this.letterNotation);
        this.row = position[0];
        this.col = position[1];
    }
    // Methods
    public void erasePiece() {
        board.getGrid()[row][col] = new Piece(" ", this);
    }

    public void setPiece(Piece newPiece) {
        board.getGrid()[row][col] = newPiece;
        newPiece.setLocation(this);
    }

    // Grabbers
    public String getLetterNotation() {
        return this.letterNotation;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public static String convertLocation(int row, int col) {
        char file = (char) ('a' + col);
        int rank = 8 - row;

        return "" + file + rank;
    }

    public static int[] convertLocation(String letterNotation) {
        int[] coordinates = new int[2];

        coordinates[1] = letterNotation.charAt(0) - 'a';
        coordinates[0] = 8 - Integer.parseInt(letterNotation.substring(1));

        return coordinates;
    }

    public Piece getPiece() {
        return this.board.getGrid()[row][col];
    }

    public Location translateLocation(int rowAdd, int colAdd) {
        if (isOutOfBounds(this.row + rowAdd, this.col + colAdd)) return null;
        return new Location(this.row + rowAdd, this.col + colAdd, board);
    }

    public static boolean isOutOfBounds(Location testLoc) {
        if (testLoc == null) return true;
        return (testLoc.row < 0 || testLoc.row > 7 || testLoc.col < 0 || testLoc.col > 7);
    }

    public static boolean isOutOfBounds(int row, int col) {
        return (row < 0 || row > 7 || col < 0 || col > 7);
    }

    public boolean isSameSquareAs(Location other) {
        if (other == null) {
            return false;
        }
        return this.row == other.row && this.col == other.col;
    }

    public ChessBoard getBoard() {return this.board;}
}
