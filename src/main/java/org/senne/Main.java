package org.senne;

import java.io.Console;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    static int circles = 0;
    static int crosses = 0;
    static int checks = 0;

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Do you want to go first? (y/n)");
        String input = scanner.nextLine();

        if (input.equals("n")) {
            doBestMove();
        }

        while (!((checkWinCircles(circles) || checkWinCrosses(crosses)) || (circles | crosses) == 511)){
            printBoardAskInput();
            if (checkWinCircles(circles) || checkWinCrosses(crosses) || (circles | crosses) == 511) {
                break;
            }
            doBestMove();
            if (checkWinCircles(circles) || checkWinCrosses(crosses) || (circles | crosses) == 511) {
                drawBoard();
            }
        }

        if (checkWinCrosses(crosses)) {
            System.out.println("You win!");
        } else if (checkWinCircles(circles)) {
            System.out.println("You lose!");
        } else {
            System.out.println("It's a draw!");
        }
    }

    static void doBestMove() {
        long time = System.currentTimeMillis();

        byte alpha = Byte.MIN_VALUE;
        byte beta = Byte.MAX_VALUE;

        byte bestMove = -1;
        byte bestEval = -1;
        for (byte i = 0; i < 9; i++) {
            if (((int) Math.pow(2, i) & (circles | crosses)) == 0) {
                byte eval = minimax(circles | ((int) Math.pow(2, i)), crosses, alpha, beta, (byte) 1, false);
                if (eval >= bestEval) {
                    bestEval = eval;
                    bestMove = i;
                }
            }
        }

        System.out.println("Time: " + (System.currentTimeMillis() - time) + "ms");
        System.out.println("Checks: " + checks);
        checks = 0;
        circles = circles | ((int) Math.pow(2, bestMove));
        System.out.println("Computer placed O at " + (bestMove + 1));
    }

    static byte minimax(int circles, int crosses, byte alpha, byte beta, byte i, boolean moveO) {
        checks++;

        if (checkWinCircles(circles)) {
            return 1;
        } else if (checkWinCrosses(crosses)) {
            return -1;
        } else if (i >= 9) {
            return 0;
        } else if ((circles | crosses) == 511) {
            return 0;
        } else if (!moveO && winsInOneCrosses(circles, crosses) > 0) {
            return (byte) (winsInOneCrosses(circles, crosses) * -1);
        } else if (moveO && winsInOneCircles(circles, crosses) > 0) {
            return winsInOneCircles(circles, crosses);
        }

        if (!moveO) {
            byte minEval = Byte.MAX_VALUE;
            for (int j = 0; j < 9; j++) {
                if (((int) Math.pow(2, j) & (circles | crosses)) == 0) {
                    byte eval = minimax(circles, crosses | ((int) Math.pow(2, j)), alpha, beta, (byte) (i + 1), true);
                    minEval = (byte) Math.min(minEval, eval);

                    beta = (byte) Math.min(beta, minEval);
                    if (minEval <= alpha) {
                        return minEval;
                    }
                }
            }
            return minEval;
        }

        if (moveO) {
            byte maxEval = Byte.MIN_VALUE;
            for (int j = 0; j < 9; j++) {
                if (((int) Math.pow(2, j) & (circles | crosses)) == 0) {
                    int eval = minimax(circles | ((int) Math.pow(2, j)), crosses, alpha, beta, (byte) (i + 1), false);
                    maxEval = (byte) Math.max(maxEval, eval);

                    alpha = (byte) Math.max(alpha, maxEval);
                    if (maxEval >= beta) {
                        return maxEval;
                    }

                }
            }
            return maxEval;
        }

        return 0;
    }

    static byte winsInOneCircles(int circles, int crosses) {
        byte wins = 0;
        for (int i = 0; i < 9; i++) {
            if (((int) Math.pow(2, i) & (circles | crosses)) == 0) {
                if (checkWinCircles(circles | ((int) Math.pow(2, i)))) {
                    wins++;
                }
            }
        }
        return wins;
    }

    static byte winsInOneCrosses(int circles, int crosses) {
        byte wins = 0;
        for (int i = 0; i < 9; i++) {
            if (((int) Math.pow(2, i) & (circles | crosses)) == 0) {
                if (checkWinCrosses(crosses | ((int) Math.pow(2, i)))) {
                    wins++;
                }
            }
        }
        return wins;
    }

    static boolean checkWinCircles(int circles) {
        int[] winConditions = {7, 56, 448, 73, 146, 292, 273, 84};
        // 7 = 111, 56 = 111000, 448 = 111000000, 73 = 1001001, 146 = 1010010, 292 = 1011100, 273 = 100010001, 84 = 1010100
        for (int winCondition : winConditions) {
            if ((circles & winCondition) == winCondition) {
                return true;
            }
        }
        return false;
    }

    static boolean checkWinCrosses(int crosses) {
        int[] winConditions = {7, 56, 448, 73, 146, 292, 273, 84};
        // 7 = 111, 56 = 111000, 448 = 111000000, 73 = 1001001, 146 = 1010010, 292 = 1011100, 273 = 100010001, 84 = 1010100
        for (int winCondition : winConditions) {
            if ((crosses & winCondition) == winCondition) {
                return true;
            }
        }
        return false;
    }

    static void printBoardAskInput() {
        drawBoard();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Where do you want to place your X?");
        int input = scanner.nextInt();
        boolean validInput = ((int) Math.pow(2, input - 1) & (circles | crosses)) == 0;

        while ((input < 1 || input > 9) || !validInput) {
            System.out.println("Invalid input. Please enter a valid number.");
            input = scanner.nextInt();
            validInput = ((int) Math.pow(2, input - 1) & (circles | crosses)) == 0;
        }

        crosses = crosses | ((int) Math.pow(2, input - 1));
    }

    static void drawBoard() {
        // 1 1 1 1 1 1 1 1 1
        // bottom right to top left

        // 1 2 4 8 16 32 64 128 256
        // top left is 1 and bottom right is 256

        String[] spots = new String[9];

        for (int i = 0; i < 9; i++) {
            if ((circles & (1 << i)) == (1 << i)) {
                spots[i] = "\033[0;31m" + "O" + "\033[0m";
            } else if ((crosses & (1 << i)) == (1 << i)) {
                spots[i] = "\033[0;32m" + "X" + "\033[0m";
            } else {
                spots[i] = Integer.toString(i + 1);
            }
        }

        String board =  "-------------------\n" +
                "|  " + spots[0] + "  |  " + spots[1] + "  |  " + spots[2] + "  |\n" +
                "-------------------\n" +
                "|  " + spots[3] + "  |  " + spots[4] + "  |  " + spots[5] + "  |\n" +
                "-------------------\n" +
                "|  " + spots[6] + "  |  " + spots[7] + "  |  " + spots[8] + "  |\n" +
                "-------------------\n";
        System.out.println(board);
    }
}