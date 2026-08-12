package GameObjects;

import Resources.PieceResources;
import java.util.ArrayList;

public class Piece {
    private String pieceLetter;
    private String pieceGraphic;
    private Location location;
    private boolean hasMoved;

    public Piece(String pieceLetter, Location location) {
        this.pieceLetter = pieceLetter;
        this.pieceGraphic = PieceResources.letterGraphicConversion.get(pieceLetter);
        this.location = location;
        this.hasMoved = false;
    }

    public ArrayList<Move> getPsuedoLegalMoves() {
        // PieceResources already handles valid board collision/captures for us!
        ArrayList<int[]> moveCheckList = PieceResources.getPieceMoveTranslations(this, true);
        ArrayList<Move> psuedoLegalMoves = new ArrayList<>();

        for (int[] m : moveCheckList) {
            Location translatedLoc = this.location.translateLocation(m[0], m[1]);

            // If the translation went out of bounds or hit an invalid square, skip it entirely
            if (translatedLoc == null || Location.isOutOfBounds(translatedLoc)) continue;

            // Check if this is a pawn reaching a promotion rank
            if (this.getPieceType().equalsIgnoreCase("p") && (translatedLoc.getRow() == 0 || translatedLoc.getRow() == 7)) {
                boolean isWhite = this.isPieceWhite();

                // Add 4 distinct moves for the 4 possible promotion branches
                psuedoLegalMoves.add(new Move(this.location, translatedLoc, isWhite ? "Q" : "q"));
                psuedoLegalMoves.add(new Move(this.location, translatedLoc, isWhite ? "R" : "r"));
                psuedoLegalMoves.add(new Move(this.location, translatedLoc, isWhite ? "B" : "b"));
                psuedoLegalMoves.add(new Move(this.location, translatedLoc, isWhite ? "N" : "n"));

                // We have completely handled this promotion square. Skip the standard add below!
                continue;
            }

            // Otherwise, treat it as a completely standard step
            psuedoLegalMoves.add(new Move(this.location, translatedLoc));
        }
        return psuedoLegalMoves;
    }

    public ArrayList<Move> getLegalMoves() {
        ArrayList<Move> legalMoves = new ArrayList<>();
        ChessBoard currentBoard = this.getBoard();

        for (Move m : this.getPsuedoLegalMoves()) {
            ChessBoard.MoveHistory history = currentBoard.makeMove(m);

            if (!ChessBoard.isKingInCheck(currentBoard, this.isPieceWhite())) {
                legalMoves.add(m);
            }
            currentBoard.unmakeMove(history);
        }
        return legalMoves;
    }

    public void setLocation(Location location) { this.location = location; }
    public String getPieceName() { return ChessBoard.printGraphic ? this.pieceGraphic : this.pieceLetter; }
    public String getPieceType() { return this.pieceLetter; }
    public boolean isPiece() { return !this.getPieceType().equals(" "); }
    public boolean isPieceWhite() { return this.pieceLetter.toUpperCase().equals(this.pieceLetter); }
    public Location getLocation() { return this.location; }
    public ChessBoard getBoard() { return this.getLocation().getBoard(); }
    public void setPieceHasMoved(boolean hasMoved) { this.hasMoved = hasMoved; }
    public boolean hasPieceMoved() { return this.hasMoved; }
}