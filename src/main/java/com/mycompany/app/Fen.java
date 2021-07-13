package com.mycompany.app;

import java.util.stream.IntStream;
import java.util.Properties;
import java.util.ArrayList;

public class Fen {
    private String fen;
    private String board;
    private String sideToMove;
    private String castling;
    private String enPassant;
    private FenBuilder fenBuilder;
    private final Properties castlingRights;
    private final Properties castleRookMoves;
    private final Properties rooks;
    private final String moveFormatMask;
    private final String pawnPromotionMask;

    Fen() {    
        setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w AHah -");

        fenBuilder = new FenBuilder();
        moveFormatMask = "([a-h][1-8]){2}";
        pawnPromotionMask = "([a-h])([27])\\1([18])([rnbq])";

        castlingRights = new Properties();  
        castlingRights.setProperty("e1g1", "H");
        castlingRights.setProperty("e8g8", "h");
        castlingRights.setProperty("e1c1", "A");
        castlingRights.setProperty("e8c8", "a");

        castleRookMoves = new Properties();
        castleRookMoves.setProperty("e1g1", "h1f1");
        castleRookMoves.setProperty("e8g8", "h8f8");
        castleRookMoves.setProperty("e1c1", "a1d1");
        castleRookMoves.setProperty("e8c8", "a8d8");

        rooks = new Properties();
        rooks.setProperty("h1", "H");
        rooks.setProperty("h8", "h");
        rooks.setProperty("a1", "A");
        rooks.setProperty("a8", "a");
    }

    public String get() {
        return this.fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
        String[] splitted = fen.split(" ");
        board = expandBoard();
        sideToMove = splitted[1];
        castling = splitted[2];
        enPassant = splitted[3];
    }

    private boolean checkMoveSignature(String move) {
        if (move.matches(moveFormatMask) || move.matches(pawnPromotionMask)) return true;

        return false;
    }

    private char findPiece(String move) {
        char piece = this.board.split("/")['8' - move.charAt(1)].charAt(move.charAt(0) - 'a');

        return piece;
    }

    private boolean ofOppositeColor(char piece) {
        String sideToMove = this.fen.split(" ")[1];

        return Character.isUpperCase(piece) ^ sideToMove.equals("w");
    }

    private int[] getDistance(String move, boolean abs) {
        int dist[] = new int[2];

        String currPos = move.substring(0, 2);
        String dstPos = move.substring(2, 4);

        dist[0] = abs ? Math.abs(dstPos.charAt(0) - currPos.charAt(0)) : dstPos.charAt(0) - currPos.charAt(0); 
        dist[1] = abs ? Math.abs(dstPos.charAt(1) - currPos.charAt(1)) : dstPos.charAt(1) - currPos.charAt(1);

        return dist;
    }

    private String expandBoard() {
        return this.fen.split(" ")[0].chars()
            .mapToObj(Character::toString).reduce("", (expanded, str) -> {
                if (str.matches("[1-8]")) {
                    expanded += "1".repeat(Integer.parseInt(str));
                } else {
                    expanded += str;
                }

                return expanded;
            });
    }

    private String packBoard(String expandedBoard) {
        return expandedBoard.chars()
            .mapToObj(Character::toString).reduce("", (packedBoard, square) -> {
                if (square.equals("1")) {
                    char lastChar = packedBoard.length() == 0 ? '\0' : packedBoard.charAt(packedBoard.length() - 1);

                    if (Character.isDigit(lastChar)) {
                        char chars[] = packedBoard.toCharArray();
                        chars[chars.length - 1] = ++lastChar;

                        packedBoard = new String(chars);
                    } else {
                        packedBoard += "1";
                    }
                } else {
                    packedBoard += square;
                }

                return packedBoard;
            });
    }

    private boolean isKingInCheck() {
        String[] splitBoard = board.split("/");
        char king = sideToMove.equals("w") ? 'K' : 'k';

        // find king
        int colN = 0;
        int rowN = 8;

        for (String row : splitBoard) {
            if ((colN = row.indexOf(king)) != -1) break;

            rowN -= 1; 
        }

        String kingCoord = new String(new StringBuilder().append((char)(colN + 97)).append(rowN));

        return isSquareUnderAttack(kingCoord);
    }

    private boolean isSquareUnderAttack(String square) {
        String[] splitBoard = board.split("/");

        for (int j = 0; j < splitBoard.length; j++) {
            String row = splitBoard[j];
            final int r = j;

            boolean isUnderAttack = IntStream.range(0, row.length())
                .filter(i -> Character.isAlphabetic(row.charAt(i)) && ofOppositeColor(row.charAt(i)))
                .mapToObj(i -> new String(new StringBuilder().append((char)(i + 97)).append(8 - r)))
                .anyMatch(coord -> validateMoveSignature(coord + square));
            
            if (isUnderAttack) return true;
        }

        return false;
    }

    private String[] getSquaresInBetween(String move) {
        ArrayList<String> squares = new ArrayList<>();
        String currPos = move.substring(0, 2);
        String dstPos = move.substring(2, 4);
        int[] dist = getDistance(move, false);

        int rowInc = dist[0] > 0 ? 1 : dist[0] == 0 ? 0 : -1;
        int colInc = dist[1] > 0 ? 1 : dist[1] == 0 ? 0 : -1;

        String pos = currPos;

        while (!pos.equals(dstPos)) {
            pos = new StringBuilder()
                .append((char)(pos.charAt(0) + rowInc))
                .append((char)(pos.charAt(1) + colInc))
                .toString();

            if (!pos.equals(dstPos)) squares.add(pos);
        }

        return squares.toArray(new String[squares.size()]);
    }

    private boolean hasPiecesInBetween(String move) {
        for (String square : getSquaresInBetween(move)) {
            if (findPiece(square) != '1') return true;
        }

        return false;
    }

    private boolean validatePawnMoveSignature(String move) {
        String currPos = move.substring(0, 2);
        String dstPos = move.substring(2, 4);

        int dist[] = getDistance(move, true);

        if ((sideToMove.equals("w")) ^ (currPos.charAt(1) > dstPos.charAt(1))) {
            if (
                (dist[0] == 0 && dist[1] == 1 && findPiece(dstPos) == '1')
                || (dist[0] == 1 && dist[1] == 1 && findPiece(dstPos) != '1' && ofOppositeColor(findPiece(dstPos)))
                ) {
                return true;
            }  else if ((dist[0] == 0 && dist[1] == 2 && (currPos.charAt(1) == '2' || currPos.charAt(1) == '7') && findPiece(dstPos) == '1' && !hasPiecesInBetween(move))) {
                // if pawn moves 2 squares forward
                this.fenBuilder.setEnPassant(dstPos);

                return true;
            } else if (dist[0] == 1 && dist[1] == 1 && findPiece(dstPos) == '1') {
                // capturing en passant                
                if (enPassant != "-") {
                    int[] pawnDist = getDistance(currPos + enPassant, true);

                    if (pawnDist[0] == 1 && pawnDist[1] == 0) {
                        this.fenBuilder.setEnPassant("-");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean validateBishopMoveSignature(String move) {
        String dstPos = move.substring(2, 4);

        int dist[] = getDistance(move, true);

        if (dist[0] == dist[1] && (findPiece(dstPos) == '1' || ofOppositeColor(findPiece(dstPos))) && !hasPiecesInBetween(move)) {
            return true;
        }

        return false;
    }

    private boolean validateRookMoveSignature(String move) {
        String currPos = move.substring(0, 2);
        String dstPos = move.substring(2, 4);

        int dist[] = getDistance(move, true);

        if ((dist[0] == 0 ^ dist[1] == 0) && (findPiece(dstPos) == '1' || ofOppositeColor(findPiece(dstPos))) && !hasPiecesInBetween(move)) {
            if (this.rooks.getProperty(currPos) != null && this.castling.indexOf(this.rooks.getProperty(currPos)) != -1) {
                String castling = this.castling.replaceAll(this.rooks.getProperty(currPos), "");
                castling = castling.length() == 0 ? "-" : castling;
                this.fenBuilder.setCastling(castling);
            }
            return true;
        }

        return false;
    }

    private boolean validateKnightMoveSignature(String move) {
        String dstPos = move.substring(2, 4);

        int dist[] = getDistance(move, true);

        if ((dist[0] == 1 && dist[1] == 2) || (dist[0] == 2 && dist[1] == 1) && (findPiece(dstPos) == '1' || ofOppositeColor(findPiece(dstPos)))) {
            return true;
        }

        return false;
    }

    private boolean validateKingMoveSignature(String move) {
        String dstPos = move.substring(2, 4);

        int dist[] = getDistance(move, true);

        if ((dist[0] == 0 && dist[1] == 1) || (dist[0] == 1 && dist[1] == 0) || (dist[0] * dist[1] == 1)) {
                if (findPiece(dstPos) == '1' || (findPiece(dstPos) != '1' && ofOppositeColor(findPiece(dstPos)))) {
                    return true;
                }
        }

        // castling logic
        String castlingSide = this.castlingRights.getProperty(move);
        if (castlingSide != null && this.castling.contains(castlingSide)) {
            if (!isKingInCheck() && !hasPiecesInBetween(move)) {
                // check squares in between for checks
                for (String square : getSquaresInBetween(move)) {
                    if (isSquareUnderAttack(square)) return false;
                }

                String castling = this.castling.replaceAll(sideToMove.equals("w") ? "AH" : "ah","");
                castling = castling.length() == 0 ? "-" : castling;
                this.fenBuilder.setCastling(castling);

                return true;
            }
        }

        return false;
    }

    private boolean validateQueenMoveSignature(String move) {
        if (validateBishopMoveSignature(move) || validateRookMoveSignature(move)) {
            return true;
        }

        return false;
    }

    private boolean validateMoveSignature(String move) {
        boolean isValid = false;
        char piece = findPiece(move);

        switch(Character.toLowerCase(piece)) {
            case 'p':
                isValid = validatePawnMoveSignature(move);
                break;
            case 'r':
                isValid = validateRookMoveSignature(move);
                break;
            case 'n':
                isValid = validateKnightMoveSignature(move);
                break;
            case 'b':
                isValid = validateBishopMoveSignature(move);
                break;
            case 'q':
                isValid = validateQueenMoveSignature(move);
                break;
            case 'k':
                isValid = validateKingMoveSignature(move);
                break;
        }

        return isValid;
    }

    private String updateBoard(String move) {
        String currPos = move.substring(0, 2);
        String dstPos = move.substring(2, 4);

        int currPosId = (currPos.charAt(0) - 'a') + 8 * (8 - Character.getNumericValue(currPos.charAt(1))) + (1 * (currPos.charAt(0) - 'a'));
        int dstPosId = (dstPos.charAt(0) - 'a') + 8 * (8 - Character.getNumericValue(dstPos.charAt(1))) + (1 * (dstPos.charAt(0) - 'a'));

        char[] board = this.board.toCharArray();
        char piece = board[currPosId];

        if (this.castlingRights.containsKey(move)) {
            // castling logic
            String rookMove = this.castleRookMoves.getProperty(move);
            String rookCurrPos = rookMove.substring(0, 2);
            String rookDstPos = rookMove.substring(2, 4);
            int rookCurrPosId = (rookCurrPos.charAt(0) - 'a') + 8 * (8 - Character.getNumericValue(rookCurrPos.charAt(1))) + (1 * (rookCurrPos.charAt(0) - 'a'));
            int rookDstPosId = (rookDstPos.charAt(0) - 'a') + 8 * (8 - Character.getNumericValue(rookDstPos.charAt(1))) + (1 * (rookDstPos.charAt(0) - 'a'));

            board[rookDstPosId] = board[rookCurrPosId];
            board[rookCurrPosId] = '1';
        } else if (move.matches(pawnPromotionMask)) {
            // pawn promotion
            piece = pawnPromotionMask.charAt(pawnPromotionMask.length() - 1);
            piece = sideToMove.equals("w") ? Character.toUpperCase(piece) : piece;
        }
        board[currPosId] = '1';
        board[dstPosId] = piece;

        return new String(board);
    }

    public void updateFen(String move) throws IllegalArgumentException, Exception {
        if (!this.checkMoveSignature(move)) {
            throw new IllegalArgumentException("Wrong signature for move " + move);
        }

        this.fenBuilder = new FenBuilder();
        char piece = findPiece(move);

        if (!Character.isLetter(piece)) {
            throw new IllegalArgumentException("No piece at given coordinates " + move);
        }
        if (sideToMove.equals("w") ^ Character.isUpperCase(piece)) {
            throw new IllegalArgumentException("Not your turn");
        }
        if (!validateMoveSignature(move)) {
            throw new IllegalArgumentException("Invalid move: " + move);
        }

        String oldBoard = this.board;
        this.board = updateBoard(move);

        if (isKingInCheck()) {
            this.board = oldBoard;

            throw new IllegalArgumentException("King is in check after move: " + move);
        }

        if (this.fenBuilder.enPassant == null) this.fenBuilder.setEnPassant("-");
        if (this.fenBuilder.castling == null) this.fenBuilder.setCastling(this.castling);
        this.fenBuilder.setBoard(packBoard(this.board));
        this.fenBuilder.setSideToMove(sideToMove.equals("w") ? "b" : "w");
        
        setFen(this.fenBuilder.build());
    }

    class FenBuilder {
        private String board;
        private String sideToMove;
        private String castling;
        private String enPassant;

        public void setBoard(String board) {
            this.board = board;
        }

        public void setSideToMove(String sideToMove) {
            this.sideToMove = sideToMove;
        }

        public void setCastling(String castling) {
            this.castling = castling;
        }

        public void setEnPassant(String enPassant) {
            this.enPassant = enPassant;
        }

        public String build() throws Exception {
            if (this.board == null) {
                throw new Exception("FenBuilder: Board not set");
            }
            if (this.sideToMove == null) {
                throw new Exception("FenBuilder: Side to move not set");
            }
            if (this.castling == null) {
                throw new Exception("FenBuilder: Castling not set");
            }
            if (this.enPassant == null) {
                throw new Exception("FenBuilder: En passant not set");
            }

            String fen = this.board + " " + this.sideToMove + " " + this.castling + " " + this.enPassant;

            return fen;
        }
    }
}
