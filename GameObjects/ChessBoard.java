package GameObjects;

import Resources.GeneralResources;
import Resources.PieceResources;
import java.util.ArrayList;

public class ChessBoard {
    private static final int LENGTH = 8;
    private static final int HEIGHT = 8;
    private static final String FEN_RESET = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private String filesCoordinates;
    public static boolean printGraphic;
    public static boolean showCoordinates;

    private String FENstring;
    private Piece[][] grid;
    private boolean whiteToPlay;
    private boolean isGameOver;
    private int totalMoves;

    private Location enPassantTargetSquare = null;
    public boolean isEvaluatingCastling = false;

    // Tracker fields for Castling Rights parsed via FEN
    private boolean whiteCanCastleKingside = false;
    private boolean whiteCanCastleQueenside = false;
    private boolean blackCanCastleKingside = false;
    private boolean blackCanCastleQueenside = false;

    public ChessBoard() {
        this.whiteToPlay = true;
        this.FENstring = FEN_RESET;
        this.filesCoordinates = "abcdefgh";
        this.grid = FENtoArray(this.FENstring);
        this.isGameOver = false;
        this.totalMoves = 0;
    }

    public ChessBoard(boolean whiteStart) {
        this.whiteToPlay = whiteStart;
        this.FENstring = FEN_RESET;
        this.filesCoordinates = (whiteToPlay) ? "abcdefgh" : "hgfedcba";
        this.isGameOver = false;
        this.grid = FENtoArray(this.FENstring);
        this.totalMoves = 0;
    }

    public ChessBoard(String FENstring, boolean whiteToPlay, int totalMoves) {
        this.whiteToPlay = whiteToPlay;
        this.FENstring = FENstring;
        this.filesCoordinates = (whiteToPlay) ? "abcdefgh" : "hgfedcba";
        this.isGameOver = false;
        this.grid = FENtoArray(this.FENstring);
        this.totalMoves = totalMoves;
    }

    public ChessBoard(ChessBoard board) {
        this.whiteToPlay = board.isWhiteToPlay();
        this.FENstring = board.FENstring;
        this.filesCoordinates = board.filesCoordinates;
        this.isGameOver = board.isGameOver;
        this.totalMoves = board.totalMoves;
        this.whiteCanCastleKingside = board.whiteCanCastleKingside;
        this.whiteCanCastleQueenside = board.whiteCanCastleQueenside;
        this.blackCanCastleKingside = board.blackCanCastleKingside;
        this.blackCanCastleQueenside = board.blackCanCastleQueenside;

        if (board.getEnPassantTargetSquare() != null) {
            this.enPassantTargetSquare = new Location(board.getEnPassantTargetSquare().getRow(), board.getEnPassantTargetSquare().getCol(), this);
        }

        this.grid = new Piece[8][8];
        Piece[][] oldGrid = board.getGrid();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece oldPiece = oldGrid[r][c];
                Location newLocation = new Location(r, c, this);
                Piece newPiece = new Piece(oldPiece.getPieceType(), newLocation);

                if (oldPiece.hasPieceMoved()) {
                    newPiece.setPieceHasMoved(true);
                }
                newLocation.setPiece(newPiece);
                this.grid[r][c] = newPiece;
            }
        }
    }

    public static class MoveHistory {
        public final Move move;
        public final Piece capturedPiece;
        public final boolean wasPieceMovedBefore;
        public final Location oldEnPassantTarget;
        public final String originalMovingPieceType;
        public final boolean wCK, wCQ, bCK, bCQ;

        public MoveHistory(Move move, Piece capturedPiece, boolean wasPieceMovedBefore, Location oldEnPassantTarget, String originalMovingPieceType, boolean wCK, boolean wCQ, boolean bCK, boolean bCQ) {
            this.move = move;
            this.capturedPiece = capturedPiece;
            this.wasPieceMovedBefore = wasPieceMovedBefore;
            this.oldEnPassantTarget = oldEnPassantTarget;
            this.originalMovingPieceType = originalMovingPieceType;
            this.wCK = wCK; this.wCQ = wCQ; this.bCK = bCK; this.bCQ = bCQ;
        }
    }

    public MoveHistory makeMove(Move move) {
        Location from = move.getFrom();
        Location to = move.getTo();

        Piece movingPiece = from.getPiece();
        Piece capturedPiece = to.getPiece();
        boolean wasMovedBefore = movingPiece.hasPieceMoved();
        Location oldEP = this.enPassantTargetSquare;
        String originalType = movingPiece.getPieceType();

        // Save castling states
        boolean wCK = whiteCanCastleKingside; boolean wCQ = whiteCanCastleQueenside;
        boolean bCK = blackCanCastleKingside; boolean bCQ = blackCanCastleQueenside;

        // 1. Core Move Execution
        from.erasePiece();
        to.setPiece(movingPiece);
        movingPiece.setPieceHasMoved(true);

        // Clear out the En Passant target square state for next turn
        this.enPassantTargetSquare = null;

        // Handle Pawn Promotion Morphing
        if (move.isPromotion()) {
            Piece promotedPiece = new Piece(move.getPromotionType(), to);
            promotedPiece.setPieceHasMoved(true);
            to.setPiece(promotedPiece);
        }

        // Setup potential new En Passant target square upon double push
        if (originalType.equalsIgnoreCase("p") && Math.abs(to.getRow() - from.getRow()) == 2) {
            int stepDirection = movingPiece.isPieceWhite() ? -1 : 1;
            this.enPassantTargetSquare = new Location(from.getRow() + stepDirection, from.getCol(), this);
        }

        // Active En Passant capture execution
        if (originalType.equalsIgnoreCase("p") && oldEP != null && to.getRow() == oldEP.getRow() && to.getCol() == oldEP.getCol()) {
            // White pawns capture upwards (so the victim is 1 row below), Black pawns capture downwards
            int victimRow = originalType.equals("P") ? to.getRow() + 1 : to.getRow() - 1;
            int victimCol = to.getCol();

            // 1. Save the captured piece so unmakeMove() can restore it if needed
            capturedPiece = this.grid[victimRow][victimCol];

            // 2. Erase the victim pawn directly from your board grid array by replacing it with an empty space piece
            this.grid[victimRow][victimCol] = new Piece(" ", new Location(victimRow, victimCol, this));
        }

        // Standard Castling King & Rook adjustments
        if (originalType.equalsIgnoreCase("k") && Math.abs(to.getCol() - from.getCol()) == 2) {
            int row = from.getRow();
            if (to.getCol() == 6) { // Kingside
                Location rookFrom = new Location(row, 7, this);
                Location rookTo = new Location(row, 5, this);
                Piece rook = rookFrom.getPiece();
                rookFrom.erasePiece();
                rookTo.setPiece(rook);
                rook.setPieceHasMoved(true);
            } else if (to.getCol() == 2) { // Queenside
                Location rookFrom = new Location(row, 0, this);
                Location rookTo = new Location(row, 3, this);
                Piece rook = rookFrom.getPiece();
                rookFrom.erasePiece();
                rookTo.setPiece(rook);
                rook.setPieceHasMoved(true);
            }
        }

        // Adjust Castling Rights if Kings/Rooks move
        if (originalType.equals("K")) { whiteCanCastleKingside = false; whiteCanCastleQueenside = false; }
        if (originalType.equals("k")) { blackCanCastleKingside = false; blackCanCastleQueenside = false; }
        if (from.getRow() == 7 && from.getCol() == 7) whiteCanCastleKingside = false;
        if (from.getRow() == 7 && from.getCol() == 0) whiteCanCastleQueenside = false;
        if (from.getRow() == 0 && from.getCol() == 7) blackCanCastleKingside = false;
        if (from.getRow() == 0 && from.getCol() == 0) blackCanCastleQueenside = false;

        this.whiteToPlay = !this.whiteToPlay;
        return new MoveHistory(move, capturedPiece, wasMovedBefore, oldEP, originalType, wCK, wCQ, bCK, bCQ);
    }

    public void unmakeMove(MoveHistory history) {
        Location from = history.move.getFrom();
        Location to = history.move.getTo();

        // Restore original piece identity if it was promoted
        Piece movingPiece = new Piece(history.originalMovingPieceType, from);
        movingPiece.setPieceHasMoved(history.wasPieceMovedBefore);

        // Restore core state
        to.setPiece(history.capturedPiece);
        from.setPiece(movingPiece);

        this.enPassantTargetSquare = history.oldEnPassantTarget;
        this.whiteCanCastleKingside = history.wCK; this.whiteCanCastleQueenside = history.wCQ;
        this.blackCanCastleKingside = history.bCK; this.blackCanCastleQueenside = history.bCQ;

        // Rollback En Passant capture execution
        if (history.originalMovingPieceType.equalsIgnoreCase("p") && to.equals(history.oldEnPassantTarget)) {
            to.erasePiece();
            int stepDirection = history.originalMovingPieceType.equals("P") ? -1 : 1;
            Location victimLoc = new Location(to.getRow() - stepDirection, to.getCol(), this);
            victimLoc.setPiece(history.capturedPiece);
        }

        // Rollback Castling configuration
        if (history.originalMovingPieceType.equalsIgnoreCase("k") && Math.abs(to.getCol() - from.getCol()) == 2) {
            int row = from.getRow();
            if (to.getCol() == 6) { // Kingside Rollback
                Location rookFrom = new Location(row, 7, this);
                Location rookTo = new Location(row, 5, this);
                Piece rook = rookTo.getPiece();
                rookTo.erasePiece();
                rookFrom.setPiece(rook);
                rook.setPieceHasMoved(false);
            } else if (to.getCol() == 2) { // Queenside Rollback
                Location rookFrom = new Location(row, 0, this);
                Location rookTo = new Location(row, 3, this);
                Piece rook = rookTo.getPiece();
                rookTo.erasePiece();
                rookFrom.setPiece(rook);
                rook.setPieceHasMoved(false);
            }
        }

        this.whiteToPlay = !this.whiteToPlay;
    }

    public Piece[][] FENtoArray(String FENstring) {
        Piece[][] newBoard = new Piece[LENGTH][HEIGHT];
        String[] sections = FENstring.split(" ");
        String piecesPart = sections[0];

        int row = 0;
        int col = 0;

        // 1. Parse Pieces Layout Grid
        for (int i = 0; i < piecesPart.length(); i++) {
            String c = piecesPart.substring(i, i+1);

            if (GeneralResources.isNumeric(c)) {
                for (int n = 0; n < Integer.parseInt(c); n++) {
                    newBoard[row][col] = new Piece(" ", new Location(row, col, this));
                    col++;
                }
            } else if (c.equals("/")) {
                row++;
                col = 0;
            } else {
                Location pieceLoc = new Location(row, col, this);
                Piece newPiece = new Piece(c, pieceLoc);

                if (c.equalsIgnoreCase("p")) {
                    if ((c.equals("P") && row == 6) || (c.equals("p") && row == 1)) {
                        newPiece.setPieceHasMoved(false);
                    } else {
                        newPiece.setPieceHasMoved(true);
                    }
                }
                newBoard[row][col] = newPiece;
                col++;
            }
        }

        // 2. Parse Active Player Color Turn
        if (sections.length > 1) {
            this.whiteToPlay = sections[1].equals("w");
        }

        // 3. Parse Castling Rights State
        if (sections.length > 2) {
            String rights = sections[2];
            this.whiteCanCastleKingside = rights.contains("K");
            this.whiteCanCastleQueenside = rights.contains("Q");
            this.blackCanCastleKingside = rights.contains("k");
            this.blackCanCastleQueenside = rights.contains("q");
        }

        // 4. Parse Active En Passant Targets
        if (sections.length > 3 && !sections[3].equals("-")) {
            this.enPassantTargetSquare = new Location(sections[3], this);
        }

        return newBoard;
    }

    public static void printBoard(ChessBoard board) {
        Piece[][] grid = board.getGrid();
        String buffer = (ChessBoard.showCoordinates) ? " " : "";

        System.out.println(buffer + " ╔═══╦═══╦═══╦═══╦═══╦═══╦═══╦═══╗");
        for (int i = 0; i < grid.length; i++) {
            if (ChessBoard.showCoordinates) {
                System.out.print(8 - i + " ║");
            } else {
                System.out.print(" ║");
            }

            for (Piece p : grid[i]) {
                if (!p.isPiece()) {
                    System.out.print("   ║");
                } else {
                    System.out.print(" " + p.getPieceName() + " ║");
                }
            }
            System.out.println();

            if (i < board.getGrid().length - 1) {
                System.out.println(buffer + " ╠═══╬═══╬═══╬═══╬═══╬═══╬═══╬═══╣");
            }
        }
        System.out.println(buffer + " ╚═══╩═══╩═══╩═══╩═══╩═══╩═══╩═══╝");

        if (ChessBoard.showCoordinates) {
            System.out.print("    ");
            for (int i = 0; i < board.filesCoordinates.length(); i++) {
                System.out.print(board.filesCoordinates.substring(i, i+1) + "   ");
            }
            System.out.println();
        }
        System.out.println(buffer + (board.whiteToPlay ? "         (White to play)" : "         (Black to play)"));
    }

    public static ArrayList<Move> getAllLegalMoves(ChessBoard board) {
        ArrayList<Move> allLegalMoves = new ArrayList<>();
        boolean whiteToMove = board.isWhiteToPlay();
        Piece[][] grid = board.getGrid();

        for (Piece[] row : grid) {
            for (Piece p : row) {
                if (p.isPiece() && p.isPieceWhite() == whiteToMove) {
                    allLegalMoves.addAll(p.getLegalMoves());
                }
            }
        }
        return allLegalMoves;
    }

    public static boolean isSquareAttacked(ChessBoard board, Location targetLoc, boolean sideToProtectIsWhite) {
        for (Piece[] row : board.getGrid()) {
            for (Piece p : row) {
                if (!p.isPiece() || p.isPieceWhite() == sideToProtectIsWhite) continue;

                if (p.getPieceType().equalsIgnoreCase("k")) {
                    int rowDiff = Math.abs(p.getLocation().getRow() - targetLoc.getRow());
                    int colDiff = Math.abs(p.getLocation().getCol() - targetLoc.getCol());
                    if (rowDiff <= 1 && colDiff <= 1) return true;
                    continue;
                }

                if (p.getPieceType().equalsIgnoreCase("p")) {
                    int upDown = p.isPieceWhite() ? -1 : 1;
                    int pRow = p.getLocation().getRow();
                    int pCol = p.getLocation().getCol();
                    if (targetLoc.getRow() == pRow + upDown && (targetLoc.getCol() == pCol - 1 || targetLoc.getCol() == pCol + 1)) {
                        return true;
                    }
                    continue;
                }

                ArrayList<int[]> attackVectors = PieceResources.getPieceMoveTranslations(p, false);
                if (attackVectors == null) continue;

                for (int[] v : attackVectors) {
                    Location attackLoc = p.getLocation().translateLocation(v[0], v[1]);
                    if (attackLoc != null && attackLoc.getLetterNotation().equals(targetLoc.getLetterNotation())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isKingInCheck(ChessBoard board, boolean colorWhite) {
        Location kingLocation = null;
        Piece[][] grid = board.getGrid();
        String kingType = colorWhite ? "K" : "k";

        outerLoop: for (Piece[] row : grid) {
            for (Piece p : row) {
                if (p.getPieceType().equals(kingType)) {
                    kingLocation = p.getLocation();
                    break outerLoop;
                }
            }
        }
        if (kingLocation == null) return false;
        return isSquareAttacked(board, kingLocation, colorWhite);
    }

    public Piece[][] getGrid() { return this.grid; }
    public boolean isWhiteToPlay() { return this.whiteToPlay; }
    public boolean isGameOver() {
        ArrayList<Move> currentLegalMoves = getAllLegalMoves(this);

        if (currentLegalMoves.size() > 0) {
            return false;
        }

        this.isGameOver = true;

        boolean isWhite = this.isWhiteToPlay();
        if (isKingInCheck(this, isWhite)) {
            String winner = isWhite ? "Black" : "White";
            System.out.println("\n>>> CHECKMATE! " + winner + " wins the game!");
        } else {
            System.out.println("\n>>> STALEMATE! The game ends in a draw.");
        }

        return true;
    }

    public Location getEnPassantTargetSquare() { return this.enPassantTargetSquare; }
    public boolean whiteCanCastleKingside() { return this.whiteCanCastleKingside; }
    public boolean whiteCanCastleQueenside() { return this.whiteCanCastleQueenside; }
    public boolean blackCanCastleKingside() { return this.blackCanCastleKingside; }
    public boolean blackCanCastleQueenside() { return this.blackCanCastleQueenside; }
}