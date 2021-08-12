package com.mycompany.app.chessboard;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Arrays;

enum Piece {
    p, P,
    r, R,
    n, N,
    b, B,
    q, Q,
    k, K;

    private static EnumMap<Piece, ArrayList<int[]>> attackPattern;
    private static EnumMap<Piece, ArrayList<int[]>> direction;

    static {
        attackPattern = new EnumMap<>(Piece.class);
        direction = new EnumMap<>(Piece.class);

        for (Piece piece : Piece.values()) {
            ArrayList<int[]> attackPatterns = new ArrayList<>();
            ArrayList<int[]> movePatterns = new ArrayList<>();

            switch(piece) {
                case p:
                case P:
                    if (piece.equals(p)) {
                        attackPatterns.add(new int[]{-1, 1});
                        attackPatterns.add(new int[]{-1, -1});

                        movePatterns.addAll(attackPatterns);
                        movePatterns.add(new int[]{-2, 0});
                        movePatterns.add(new int[]{-1, 0});

                    } else {
                        attackPatterns.add(new int[]{1, 1});
                        attackPatterns.add(new int[]{-1, 1});
                        
                        movePatterns.addAll(attackPatterns);
                        movePatterns.add(new int[]{2, 0});
                        movePatterns.add(new int[]{1, 0});
                    }
                    break;
                case r:
                case R:
                    attackPatterns.add(new int[]{1, 0});
                    attackPatterns.add(new int[]{-1, 0});
                    attackPatterns.add(new int[]{0, 1});
                    attackPatterns.add(new int[]{0, -1});

                    movePatterns.addAll(attackPatterns);
                    break;
                case n:
                case N:
                    attackPatterns.add(new int[]{1, 2});
                    attackPatterns.add(new int[]{-1, -2});
                    attackPatterns.add(new int[]{1, -2});
                    attackPatterns.add(new int[]{-1, 2});

                    attackPatterns.add(new int[]{2, 1});
                    attackPatterns.add(new int[]{-2, -1});
                    attackPatterns.add(new int[]{2, -1});
                    attackPatterns.add(new int[]{-2, 1});

                    movePatterns.addAll(attackPatterns);
                    break;
                case b:
                case B:
                    attackPatterns.add(new int[]{1, 1});
                    attackPatterns.add(new int[]{-1, -1});
                    attackPatterns.add(new int[]{1, -1});
                    attackPatterns.add(new int[]{-1, 1});

                    movePatterns.addAll(attackPatterns);
                    break;
                case q:
                case Q:
                    // bishop moves
                    attackPatterns.add(new int[]{1, 1});
                    attackPatterns.add(new int[]{-1, -1});
                    attackPatterns.add(new int[]{1, -1});
                    attackPatterns.add(new int[]{-1, 1});
                    // rook moves
                    attackPatterns.add(new int[]{1, 0});
                    attackPatterns.add(new int[]{-1, 0});
                    attackPatterns.add(new int[]{0, 1});
                    attackPatterns.add(new int[]{0, -1});

                    movePatterns.addAll(attackPatterns);
                    break;
                case k:
                case K:                    
                    attackPatterns.add(new int[]{1, 0});
                    attackPatterns.add(new int[]{-1, 0});
                    attackPatterns.add(new int[]{0, 1});
                    attackPatterns.add(new int[]{0, -1});

                    attackPatterns.add(new int[]{1, 1});
                    attackPatterns.add(new int[]{-1, -1});
                    attackPatterns.add(new int[]{1, -1});
                    attackPatterns.add(new int[]{-1, 1});

                    movePatterns.addAll(attackPatterns);
                    break;
            }

            attackPattern.put(piece, attackPatterns);
            direction.put(piece, movePatterns);
        }
    }

    public static ArrayList<int[]> getAttackPattern(Piece piece) {
        return attackPattern.get(piece);
    }

    public static Piece convertFromChar(char piece) {
        return Piece.valueOf(Character.toString(piece));
    }

    public static boolean isPatternReccurent(Piece piece) {
        switch(piece) {
            case P:
            case p:
            case K:
            case k:
            case N:
            case n:
                return false;
            default:
                return true;
        }
    }

    public static int[] getMoveDirection(Piece piece, int[] currPos, int[] dstPos) {
        int[] moveDirection = new int[2];

        int currRow = currPos[0];
        int currCol = currPos[1];

        int dstRow = dstPos[0];
        int dstCol = dstPos[1];

        if (dstCol < 0 || dstRow < 0 || currCol < 0 || currRow < 0) {
            return moveDirection;
        }

        // int[] vector = new int[]{ dstCol - currCol, dstRow - currRow };
        int[] vector = new int[]{ dstRow - currRow, dstCol - currCol };

        int[] vectorAbs = new int[]{ Math.abs(vector[0]), Math.abs(vector[1]) };
        int[] vectorDirection = new int[]{ vector[0] >= 0 ? 1 : -1, vector[1] >= 0 ? 1 : -1 };

        boolean isReccurent = isPatternReccurent(piece);

        for (int[] d : direction.get(piece)) {
            int[] normalizedVector = !isReccurent ? vector : new int[]{
                vectorAbs[0] / (d[0] == 0 || vectorAbs[0] == 0 ? 1 : (vectorAbs[0] / Math.abs(d[0]))) * vectorDirection[0],                
                vectorAbs[1] / (d[1] == 0 || vectorAbs[1] == 0 ? 1 : (vectorAbs[1] / Math.abs(d[1]))) * vectorDirection[1],
            };

            if (Arrays.equals(d, normalizedVector)) {
                moveDirection = d;

                break;
            }
        }

        return moveDirection;
    }

    public boolean isKing() {
        return this.equals(Piece.k) || this.equals(Piece.K);
    }

    public boolean isPawn() {
        return this.equals(Piece.p) || this.equals(Piece.P);
    }

    public boolean isRook() {
        return this.equals(Piece.r) || this.equals(Piece.R);
    }
}
