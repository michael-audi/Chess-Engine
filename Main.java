import GameObjects.ChessBoard;
import GameObjects.Location;
import GameObjects.Move;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ChessBoard.showCoordinates = true;
        ChessBoard.printGraphic = false;

        ChessBoard board = new ChessBoard();

        PVP();
    }

    public static void PVP() {
        ChessBoard board = new ChessBoard(true);
        Scanner scanner = new Scanner(System.in);

        System.out.println("       WELCOME TO PvP CHESS     ");
        System.out.println("Move format example: 'e2e4' or 'b7b8q' for promotions.");
        System.out.println("Type 'quit' at any time to end the game.\n");

        while (!board.isGameOver()) {
            ChessBoard.printBoard(board);

            String activePlayerColor = board.isWhiteToPlay() ? "White" : "Black";
            System.out.print("\n[" + activePlayerColor + "'s Turn] Enter your move: ");
            String input = scanner.nextLine().trim().toLowerCase();

            // Allow exit
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                System.out.println("Game ended by players. Thanks for playing!");
                break;
            }

            // Standard input length filtering (e.g., "e2e4" is 4 characters, "b7b8q" is 5 characters)
            if (input.length() < 4 || input.length() > 5) {
                System.out.println(">>> Invalid format! Please enter moves in a format like 'e2e4'.");
                continue;
            }

            // Extract coordinates from text string
            String fromStr = input.substring(0, 2);
            String toStr = input.substring(2, 4);

            // Bounds safety validation to prevent crashing on garbage inputs (like 'z9x4')
            char fromFile = fromStr.charAt(0);
            char fromRank = fromStr.charAt(1);
            char toFile = toStr.charAt(0);
            char toRank = toStr.charAt(1);

            if (fromFile < 'a' || fromFile > 'h' || fromRank < '1' || fromRank > '8' ||
                    toFile < 'a' || toFile > 'h' || toRank < '1' || toRank > '8') {
                System.out.println(">>> Coordinates out of bounds! Use a-h and 1-8.");
                continue;
            }

            Location fromLocation = new Location(fromStr, board);
            Location toLocation = new Location(toStr, board);
            Move playerMove;

            // Promotion
            if (input.length() == 5) {
                String promoType = input.substring(4, 5).toUpperCase();
                playerMove = new Move(fromLocation, toLocation, promoType);
            } else {
                playerMove = new Move(fromLocation, toLocation);
            }

            // --- FULL RULE LEGALITY CHECK ---
            if (playerMove.isMoveLegal(board)) {
                board.makeMove(playerMove);
                System.out.println(">>> Move accepted: " + playerMove);
                System.out.println("-----------------------------------------");
            } else {
                System.out.println(">>> Illegal move. Try again.");
                System.out.println("-----------------------------------------");
            }
        }

        scanner.close();
    }
}