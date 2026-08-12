package Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneralResources {
    // Adapted from online
    public static boolean isNumeric(String str) {
        if (str == null) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Generated with AI --> simply generates a random FEN string (random board)
    public static String generateRandomFenPositions() {
        char[][] board = new char[8][8];
        List<Character> pieces = new ArrayList<>();

        // 1. Define the piece pool (standard set minus kings)
        String pool = "QRBNPqrbnp";
        for (char c : pool.toCharArray()) {
            pieces.add(c);
            pieces.add(c);
            pieces.add(c);
            pieces.add(c);
        }

        // 2. Shuffle and pick a random number of pieces (e.g., 5 to 20 pieces)
        Collections.shuffle(pieces);
        int pieceCount = (int) (Math.random() * 20) + 20;
        List<Character> activePieces = new ArrayList<>(pieces.subList(0, pieceCount));

        // 3. Always add the Kings
        activePieces.add('K');
        activePieces.add('k');
        Collections.shuffle(activePieces);

        // 4. Place pieces on the board randomly
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 64; i++) slots.add(i);
        Collections.shuffle(slots);

        for (char piece : activePieces) {
            int pos = slots.remove(0);
            board[pos / 8][pos % 8] = piece;
        }

        // 5. Convert board array to FEN string
        StringBuilder fen = new StringBuilder();
        for (int r = 0; r < 8; r++) {
            int emptyCount = 0;
            for (int c = 0; c < 8; c++) {
                if (board[r][c] == 0) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(board[r][c]);
                }
            }
            if (emptyCount > 0) fen.append(emptyCount);
            if (r < 7) fen.append('/');
        }

        return fen.toString();
    }
}
