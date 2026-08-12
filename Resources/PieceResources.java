package Resources;

import GameObjects.ChessBoard;
import GameObjects.Location;
import GameObjects.Piece;

import static java.util.Map.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class PieceResources {
    public static final Map<String, Integer> pieceValue = Map.ofEntries(
            entry("K", 0), entry("k", 0),
            entry("Q", 9), entry("q", -9),
            entry("R", 5), entry("r", -5),
            entry("B", 3), entry("b", -3),
            entry("N", 3), entry("n", -3),
            entry("P", 1), entry("p", -1),

            entry("♚", 0), entry("♔", 0),
            entry("♛", 9), entry("♕", -9),
            entry("♜", 5), entry("♖", -5),
            entry("♝", 3), entry("♗", -3),
            entry("♞", 3), entry("♘", -3),
            entry("♟", 1), entry("♙", -1)
    );

    public static final Map<String, String> letterGraphicConversion = Map.ofEntries(
            entry("K", "♚"), entry("k", "♔"),
            entry("Q", "♛"), entry("q", "♕"),
            entry("R", "♜"), entry("r", "♖"),
            entry("B", "♝"), entry("b", "♗"),
            entry("N", "♞"), entry("n", "♘"),
            entry("P", "♟"), entry("p", "♙"),
            entry(" ", " ")
    );

    public static ArrayList<int[]> kingMoves = new ArrayList<>(Arrays.asList(
            new int[]{1, -1}, new int[]{1, 0}, new int[]{1, 1},
            new int[]{0, -1},                  new int[]{0, 1},
            new int[]{-1, -1}, new int[]{-1, 0}, new int[]{-1, 1})
    );

    public static ArrayList<int[]> knightMoves = new ArrayList<>(Arrays.asList(
            new int[]{-1, -2}, new int[]{-2, -1}, new int[]{-2, 1}, new int[]{-1, 2},
            new int[]{1, -2}, new int[]{2, -1}, new int[]{2, 1}, new int[]{1, 2})
    );

    public static ArrayList<int[]> getPieceMoveTranslations(Piece piece, boolean includeCastling) {
        String type = piece.getPieceType();
        Location loc = piece.getLocation();

        if (!piece.isPiece()) return new ArrayList<int[]>();

        else if (type.equalsIgnoreCase("k")) {
            ArrayList<int[]> moves = new ArrayList<>();

            // Standard King Steps
            for (int[] m : kingMoves) {
                Location translatedLoc = loc.translateLocation(m[0], m[1]);

                if (!Location.isOutOfBounds(translatedLoc)) {
                    if (!translatedLoc.getPiece().isPiece() || translatedLoc.getPiece().isPieceWhite() != piece.isPieceWhite()) {
                        moves.add(m);
                    }
                }
            }

            // --- CASTLING GENERATION ---
            if (includeCastling && !piece.hasPieceMoved() && !ChessBoard.isKingInCheck(piece.getBoard(), piece.isPieceWhite())) {
                ChessBoard board = piece.getBoard();
                int row = loc.getRow();

                // 1. KINGSIDE CASTLING
                Location fSquare = new Location(row, 5, board);
                Location gSquare = new Location(row, 6, board);
                Piece kingsideRook = new Location(row, 7, board).getPiece();

                if (!fSquare.getPiece().isPiece() && !gSquare.getPiece().isPiece()) {
                    if (kingsideRook.getPieceType().equalsIgnoreCase("r") && !kingsideRook.hasPieceMoved()) {
                        boolean pathIsSafe = !ChessBoard.isSquareAttacked(board, fSquare, piece.isPieceWhite());
                        if (pathIsSafe) {
                            moves.add(new int[]{0, 2});
                        }
                    }
                }

                // 2. QUEENSIDE CASTLING
                Location dSquare = new Location(row, 3, board);
                Location cSquare = new Location(row, 2, board);
                Location bSquare = new Location(row, 1, board);
                Piece queensideRook = new Location(row, 0, board).getPiece();

                if (!dSquare.getPiece().isPiece() && !cSquare.getPiece().isPiece() && !bSquare.getPiece().isPiece()) {
                    if (queensideRook.getPieceType().equalsIgnoreCase("r") && !queensideRook.hasPieceMoved()) {
                        boolean pathIsSafe = !ChessBoard.isSquareAttacked(board, dSquare, piece.isPieceWhite());
                        if (pathIsSafe) {
                            moves.add(new int[]{0, -2});
                        }
                    }
                }
            }

            return moves;
        } else if (type.equalsIgnoreCase("n")) {
            ArrayList<int[]> moves = new ArrayList<>();

            for (int[] m : knightMoves) {
                Location translatedLoc = loc.translateLocation(m[0], m[1]);

                if (!Location.isOutOfBounds(translatedLoc)) {
                    if (!translatedLoc.getPiece().isPiece() || translatedLoc.getPiece().isPieceWhite() != piece.isPieceWhite()) {
                        moves.add(m);
                    }
                }
            }

            return moves;
        } else if (type.equalsIgnoreCase("q")) {
            ArrayList<int[]> moves = new ArrayList<>();

            for (int i = 0; i <= 7; i++) {
                moves.addAll(castTranslationArrow(piece, i));
            }

            return moves;
        } else if (type.equalsIgnoreCase("r")) {
            ArrayList<int[]> moves = new ArrayList<>();

            for (int i = 0; i <= 6; i += 2) {
                moves.addAll(castTranslationArrow(piece, i));
            }

            // Faulty castling logic entirely removed from Rook block to fix infinite loops
            return moves;
        } else if (type.equalsIgnoreCase("b")) {
            ArrayList<int[]> moves = new ArrayList<>();

            for (int i = 1; i <= 7; i += 2) {
                moves.addAll(castTranslationArrow(piece, i));
            }

            return moves;
        } else if (type.equalsIgnoreCase("p")) {
            ArrayList<int[]> moves = new ArrayList<>();
            int upDown = piece.isPieceWhite() ? -1 : 1;

            // 1-Square Forward
            Location oneForward = loc.translateLocation(upDown, 0);
            if (oneForward != null && !oneForward.getPiece().isPiece()) {
                moves.add(new int[]{upDown, 0});

                // 2-Squares Forward
                Location twoForward = loc.translateLocation(upDown * 2, 0);
                if (twoForward != null && !piece.hasPieceMoved() && !twoForward.getPiece().isPiece()) {
                    moves.add(new int[]{upDown * 2, 0});
                }
            }

            // Diagonal Captures
            Location diagLeft = loc.translateLocation(upDown, -1);
            if (diagLeft != null && diagLeft.getPiece().isPiece() && diagLeft.getPiece().isPieceWhite() != piece.isPieceWhite()) {
                moves.add(new int[]{upDown, -1});
            }

            Location diagRight = loc.translateLocation(upDown, 1);
            if (diagRight != null && diagRight.getPiece().isPiece() && diagRight.getPiece().isPieceWhite() != piece.isPieceWhite()) {
                moves.add(new int[]{upDown, 1});
            }

            Location epTarget = piece.getBoard().getEnPassantTargetSquare();
            if (epTarget != null) {
                if (diagLeft != null && diagLeft.isSameSquareAs(epTarget)) {
                    moves.add(new int[]{upDown, -1});
                }
                if (diagRight != null && diagRight.isSameSquareAs(epTarget)) {
                    moves.add(new int[]{upDown, 1});
                }
            }
            return moves;
        }

        return null;
    }

    // Direction --> 0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, 6 = W, 7 = NW
    public static ArrayList<int[]> castTranslationArrow(Piece piece, int direction) {
        Location loc = piece.getLocation();
        ArrayList<int[]> moves = new ArrayList<>();
        int[] stepDirection = new int[2];

        if (direction < 2 || direction >= 7) stepDirection[0] = -1;
        else if (direction >= 3 && direction <= 5) stepDirection[0] = 1;

        if (direction >= 1 && direction <= 3) stepDirection[1] = 1;
        else if (direction >= 5 && direction <= 7) stepDirection[1] = -1;

        Location translatedLoc = loc.translateLocation(stepDirection[0], stepDirection[1]);

        while (!Location.isOutOfBounds(translatedLoc)) {
            Piece targetPiece = translatedLoc.getPiece();
            if (!targetPiece.isPiece()) {
                moves.add(new int[]{translatedLoc.getRow() - loc.getRow(), translatedLoc.getCol() - loc.getCol()});
            } else if (targetPiece.isPieceWhite() != piece.isPieceWhite()) {
                moves.add(new int[]{translatedLoc.getRow() - loc.getRow(), translatedLoc.getCol() - loc.getCol()});
                break;
            } else {
                break;
            }

            translatedLoc = translatedLoc.translateLocation(stepDirection[0], stepDirection[1]);
        }

        return moves;
    }
}