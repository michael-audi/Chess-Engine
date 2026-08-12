package Resources;

import GameObjects.ChessBoard;
import GameObjects.Move;
import GameObjects.Piece;

import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;

public class ChessBot {
    // Simple eval function that simply adds up chess pieces to see who is winning
    /* TODO
    * Piece Activity: Are your pieces actively controlling the center, or are they trapped behind your own pawns?
    * Active pieces controlling space are heavily favored.
    * King Safety: Is your king safe behind a wall of pawns, or is the opponent attacking your exposed king? King safety can outweigh a material advantage.
    * Pawn Structure: Look for structural weaknesses like doubled pawns (two pawns on the same file) or isolated pawns (pawns with no friendly pawns defending them).
    * Board Control: A player who controls more territory or has open lines for their rooks and bishops holds a space advantage.
    * */
    public static double getEvaluation(ChessBoard board) {
        double eval = 0;
        Piece[][] grid = board.getGrid();

        for (Piece[] row : grid) {
            for (Piece p : row) {
                if (p.isPiece() && !p.getPieceType().equalsIgnoreCase("k")) {
                    eval += PieceResources.pieceValue.get(p.getPieceName());
                }
            }
        }

        return eval;
    }
}
