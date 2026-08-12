package GameObjects;

import java.util.ArrayList;

public class Move {
    private Location from;
    private Location to;
    private String promotionType; // Stores "Q", "R", "B", "N" or null if standard move

    public Move(Location from, Location to) {
        this.from = from;
        this.to = to;
        this.promotionType = null;
    }

    public Move(Location from, Location to, String promotionType) {
        this.from = from;
        this.to = to;
        this.promotionType = promotionType;
    }

    public boolean isMoveLegal(ChessBoard board) {
        // 1. Safety check: Ensure coordinates aren't out of bounds
        if (Location.isOutOfBounds(this.from) || Location.isOutOfBounds(this.to)) {
            return false;
        }

        // 2. Fetch the actual piece residing at the 'from' location on this active board
        Piece movingPiece = board.getGrid()[this.from.getRow()][this.from.getCol()];

        // If there is no piece on the starting square, or it is the wrong color's turn, it's illegal
        if (!movingPiece.isPiece() || movingPiece.isPieceWhite() != board.isWhiteToPlay()) {
            return false;
        }

        // 3. Obtain all verified legal moves for this specific piece
        ArrayList<Move> legalMoves = movingPiece.getLegalMoves();

        // 4. Look for a matching move in the piece's legal options list
        for (Move legalMove : legalMoves) {
            // Check if coordinates match
            if (legalMove.getFrom().getRow() == this.from.getRow() &&
                    legalMove.getFrom().getCol() == this.from.getCol() &&
                    legalMove.getTo().getRow() == this.to.getRow() &&
                    legalMove.getTo().getCol() == this.to.getCol()) {

                // If it is a promotion move, make sure the promotion piece selection types match too
                if (this.isPromotion() || legalMove.isPromotion()) {
                    if (this.promotionType != null && this.promotionType.equalsIgnoreCase(legalMove.getPromotionType())) {
                        return true;
                    }
                    continue; // Skip if destination matches but promotion selection types differ
                }

                return true; // Found a matching standard legal move!
            }
        }

        return false; // No legal move generation matched this move
    }

    public Location getFrom() {
        return this.from;
    }

    public Location getTo() {
        return this.to;
    }

    public String getPromotionType() {
        return this.promotionType;
    }

    public boolean isPromotion() {
        return this.promotionType != null;
    }

    @Override
    public String toString() {
        if (!Location.isOutOfBounds(this.from) && !Location.isOutOfBounds(this.to)) {
            String notation = this.from.getLetterNotation() + this.to.getLetterNotation();
            if (isPromotion()) {
                notation += this.promotionType.toLowerCase();
            }
            return notation;
        }
        return "invalid move.";
    }
}