package com.mycompany.app.chessboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.mycompany.app.exceptions.IllegalMoveException;

public class Chessboard {
    private char[][] board;
    private boolean whiteToMove;
    private String whiteKingPos;
    private String blackKingPos;
    private String enPassant;
    private MoveType moveType;
    private boolean isMate;
    private final String moveFormatMask = "([a-h][1-8]){2}";
    private final String pawnPromotionMask = "([a-h])([27])\\1([18])([rnbq])";
    private Properties castlingRights;
    
    public Chessboard(String fen) {
        this.board = new char[8][8];

        fromFen(fen);

        String splitFen[] = fen.split(" ");

        this.whiteToMove = splitFen[1].equals("w");
        this.enPassant = splitFen[3];
        this.whiteKingPos = findKing(true);
        this.blackKingPos = findKing(false);
        this.isMate = isKingMated();

        String castling = splitFen[2];

        this.castlingRights = new Properties();
        if (castling.contains("K")) {
            this.castlingRights.setProperty("e1g1", "h1f1");
        }
        if (castling.contains("k")) {
            this.castlingRights.setProperty("e8g8", "h8f8");
        }
        if (castling.contains("Q")) {
            this.castlingRights.setProperty("e1c1", "a1d1");
        }
        if (castling.contains("q")) {
            this.castlingRights.setProperty("e8c8", "a8d8");
        }
    }

    public boolean getSideToMove() {
        return this.whiteToMove;
    }

    public String getEnPassant() {
        return this.enPassant;
    }

    public char[][] getBoard() {
        return this.board;
    }

    public Properties getCastlingRights() {
        return this.castlingRights;
    }

    private void fromFen(String fen) {
        String[] fenBoard = fen.split(" ")[0].split("/");

        for (int rowN = 0; rowN < 8; rowN++) {
            char[] row = fenBoard[rowN].toCharArray();
            int colN = 0;
            for (char ch : row) {
                if (Character.isDigit(ch)) {
                    colN += Character.getNumericValue(ch);
                } else {
                    this.board[7 - rowN][colN++] = ch;
                }
            }
        }
    }

    private String getCurrPos(String move) {
        return move.substring(0, 2);
    }

    private String getDstPos(String move) {
        return move.substring(2, 4);
    }

    private int[] getPosIds(String pos) {
        int[] ids = new int[2];

        int row = Character.getNumericValue(pos.charAt(1)) - 1;
        int col = pos.charAt(0) - 'a';

        ids[0] = row;
        ids[1] = col;

        return ids;
    }

    private  boolean isWhite(char piece) {
        return Character.isUpperCase(piece);
    }

    // Returns a board 8 x 8 with boolean values.
    // If value is true, than means that this square is under attack of a side which is to move after the current move is made
    // This method is needed to check for checks/mates & castling privilegies
    // Note! the logic treats enemy king as an empty square; also own pieces are also treated as squares which can be attacked if such a piece is protected by another one
    // squares past the enemy king will be also checked till the encounter of the first enemy piece
    private boolean[][] getCheckBoard() {
        boolean[][] checkBoard = new boolean[8][8];
        
        for (int rowN = 0; rowN < this.board.length; rowN++) {
            for (int colN = 0; colN < this.board[rowN].length; colN++) {
                if (pieceExists(rowN, colN)) {
                    char charPiece = findPiece(rowN, colN);

                    if (ofOppositeColor(charPiece)) {
                        Piece piece = Piece.convertFromChar(charPiece);

                        boolean isPatternRecurrent = Piece.isPatternReccurent(piece);
                        ArrayList<int[]> attackPatterns = Piece.getAttackPattern(piece);

                        for (int[] attackPattern : attackPatterns) {
                            int rowInc = attackPattern[0];
                            int colInc = attackPattern[1];

                            int newRow = rowN;
                            int newCol = colN;

                            while(true) {
                                newRow += rowInc;
                                newCol += colInc;

                                if (newRow < this.board.length && newCol < this.board.length && newRow >= 0 && newCol >= 0) {
                                    char pieceAtNewCoords = findPiece(newRow, newCol);
                                    if (pieceExists(pieceAtNewCoords)) {
                                        if (!ofOppositeColor(pieceAtNewCoords) && (pieceAtNewCoords != 'k' || pieceAtNewCoords != 'K') || (ofOppositeColor(pieceAtNewCoords))) {
                                            checkBoard[newRow][newCol] = true;
                                            break;
                                        } else {
                                            break;
                                        }
                                    } else {
                                        checkBoard[newRow][newCol] = true;
                                    }
                                } else {
                                    break;
                                }

                                if (!isPatternRecurrent) break;
                            }
                        }
                    }
                }
            }
        }

        return checkBoard;
    }

    private boolean[][] getCheckBoard(boolean forWhite) {
        boolean[][] checkBoard = new boolean[8][8];

        for (int rowN = 0; rowN < this.board.length; rowN++) {
            for (int colN = 0; colN < this.board[rowN].length; colN++) {
                if (pieceExists(rowN, colN)) {
                    char charPiece = findPiece(rowN, colN);

                    boolean pieceOfOppositeColor = isWhite(charPiece) ^ forWhite;

                    if (pieceOfOppositeColor) {
                        Piece piece = Piece.convertFromChar(charPiece);

                        boolean isPatternRecurrent = Piece.isPatternReccurent(piece);
                        ArrayList<int[]> attackPatterns = Piece.getAttackPattern(piece);

                        for (int[] attackPattern : attackPatterns) {
                            int rowInc = attackPattern[0];
                            int colInc = attackPattern[1];

                            int newRow = rowN;
                            int newCol = colN;

                            while(true) {
                                newRow += rowInc;
                                newCol += colInc;

                                if (newRow < this.board.length && newCol < this.board.length && newRow >= 0 && newCol >= 0) {
                                    char pieceAtNewCoords = findPiece(newRow, newCol);
                                    boolean pieceAtNewCoordsOfOppositeColor = !(isWhite(pieceAtNewCoords) ^ forWhite);

                                    if (pieceExists(pieceAtNewCoords)) {
                                        if (pieceAtNewCoordsOfOppositeColor && (pieceAtNewCoords == 'k' || pieceAtNewCoords == 'K')) {
                                            checkBoard[newRow][newCol] = true;
                                        } else {
                                            break;
                                        }
                                    } else {
                                        checkBoard[newRow][newCol] = true;
                                    }
                                } else {
                                    break;
                                }

                                if (!isPatternRecurrent) break;
                            }
                        }
                    }
                }
            }
        }

        return checkBoard;
    }

    private boolean pieceExists(String pos) {
        return findPiece(pos) != '\0';
    }

    private boolean pieceExists(int rowN, int colN) {
        return this.board[rowN][colN] != '\0';
    }

    private boolean pieceExists(char piece) {
        return piece != '\0';
    }

    private boolean ofOppositeColor(char piece) {
        return Character.isUpperCase(piece) ^ this.whiteToMove;
    }

    private boolean ofOppositeColor(Piece piece) {
        return ofOppositeColor(piece.name().charAt(0));
    }

    private char findPiece(String pos) {
        int[] ids = getPosIds(pos);
        int row = ids[0];
        int col = ids[1];

        return findPiece(row, col);
    }

    private char findPiece(int rowN, int colN) {
        return this.board[rowN][colN];
    }

    private String findKing(boolean white) {
        String pos = "";
        char king = white ? 'K' : 'k';
        char rank = '1';

        for (int rowN = 0; rowN < this.board.length; rowN++) {
            char let = 'a';

            for (int colN = 0; colN < this.board[rowN].length; colN++) {
                if (this.board[rowN][colN] == king) {
                    pos = new String(new char[]{ let, rank });
                    return pos;
                } else {
                    let++;
                }
            }
            rank++;
        }

        return pos;
    }

    private boolean validateCastling(String move) {
        if (this.castlingRights.containsKey(move)) {
            try {
                if (!hasPiecesInBetween(move) && !hasSquaresUnderAttack(move)) {
                    return true;
                }
            } catch (IllegalMoveException e) {}
        }

        return false;
    }

    private boolean validateEnPassant(String move) {
        String dstPos = getDstPos(move);

        return this.enPassant.equals(dstPos);
    }

    private boolean validatePromotion(String move) {
        String currPos = getCurrPos(move);
        String dstPos = getDstPos(move);

        Piece piece = Piece.convertFromChar(findPiece(currPos));

        int[] currPosIds = getPosIds(currPos);
        int[] dstPosIds = getPosIds(dstPos);

        int[] vector = Piece.getMoveDirection(piece, currPosIds, dstPosIds);
        
        if (vector[0] == 0 && vector[1] != 0) {
            if (!pieceExists(dstPos)) return true;
        }

        return false;
    }

    private boolean validateMove(String move) throws IllegalMoveException {
        if (!(move.matches(this.moveFormatMask) || move.matches(this.pawnPromotionMask))) {
            throw new IllegalMoveException("Move not recognized");
        }

        String currPos = getCurrPos(move);
        String dstPos = getDstPos(move);

        if (!pieceExists(currPos)) {
            throw new IllegalMoveException("Square " + currPos + " is empty");
        }

        Piece piece = Piece.convertFromChar(findPiece(currPos));
        
        if (ofOppositeColor(piece)) {
            throw new IllegalMoveException("Not your turn. You play as " + (this.whiteToMove ? "white" : "black"));
        }

        // check unordinary cases: castling, en passant & promotion
        if (piece.isKing() && this.castlingRights.containsKey(move)) {
            if (validateCastling(move)) {
                this.moveType = MoveType.CASTLING;

                return true;
            }
        } else if (piece.isPawn()) {
            if (validateEnPassant(move)) {
                this.moveType = MoveType.EN_PASSANT;
                return true;
            }

            if (validatePromotion(move)) {
                this.moveType = MoveType.PROMOTION;
                return true;
            }
        }

        int[] currPosIds = getPosIds(currPos);
        int[] dstPosIds = getPosIds(dstPos);
        
        int[] vector = Piece.getMoveDirection(piece, currPosIds, dstPosIds);
        if (vector[0] != 0 || vector[1] != 0) {
            if (!hasPiecesInBetween(move)) {
                if (piece.isPawn() && Math.abs(vector[0]) == 2) {
                    this.moveType = MoveType.PAWN_TWO_SQUARES;
                } else {
                    this.moveType = MoveType.REGULAR;
                }

                return true;
            }
        }

        return false;
    }

    private ArrayList<int[]> getSquaresInBetween(String move) throws IllegalMoveException {
        ArrayList<int[]> squares = new ArrayList<>();

        String currPos = getCurrPos(move);
        String dstPos = getDstPos(move);
        
        int[] currPosIds = getPosIds(currPos);
        int row = currPosIds[0];
        int col = currPosIds[1];

        int[] dstPosIds = getPosIds(dstPos);
        int dstRow = dstPosIds[0];
        int dstCol = dstPosIds[1];

        if (row == dstRow || col == dstCol || ((Math.abs(row - dstRow)) == (Math.abs(col - dstCol)))) {
            int[] direction = new int[]{
                row > dstRow ? -1 : row == dstRow ? 0 : 1,
                col > dstCol ? -1 : col == dstCol ? 0 : 1,
            };
            
            int rowInc = direction[0];
            int colInc = direction[1];
    
            int newRow = row + rowInc;
            int newCol = col + colInc;
    
            while (newCol != dstCol && newRow != dstRow) {
                squares.add(new int[]{ newRow, newCol });
    
                newCol += colInc;
                newRow += rowInc;
            }
    
            return squares;
        } else {
            throw new IllegalMoveException("Can only calculate vertiacl/horizontal/diagonal moves");
        }

        // Piece piece = Piece.convertFromChar(findPiece(currPos));

        // int[] direction = Piece.getMoveDirection(piece, currPosIds, dstPosIds);
        // if (direction[0] == 0 && direction[1] == 0) {
        //     throw new IllegalMoveException("Cannot move piece " + piece + " from " + currPos + " to " + dstPos);
        // }
    }

    private boolean hasSquaresUnderAttack(String move) throws IllegalMoveException {
        boolean[][] checkboard = getCheckBoard();

        String currPos = getCurrPos(move);
        String dstPos = getDstPos(move);
        
        int[] currPosIds = getPosIds(currPos);
        int[] dstPosIds = getPosIds(dstPos);

        ArrayList<int[]> coords = getSquaresInBetween(move);
        coords.add(currPosIds);
        coords.add(dstPosIds);

        for (int[] coord : coords) {
            if (checkboard[coord[0]][coord[1]]) return true;
        }

        return false;
    }

    private boolean hasPiecesInBetween(String move) throws IllegalMoveException {
        for (int[] coords : getSquaresInBetween(move)) {
            if (pieceExists(coords[0], coords[1])) return true;
        }
        
        return false;
    }

    private boolean isKingInCheck() {
        boolean[][] checkBoard = getCheckBoard();

        String kingPos = findKing(this.whiteToMove);
        int[] kingPosIds = getPosIds(kingPos);

        return checkBoard[kingPosIds[0]][kingPosIds[1]];
    }

    private boolean isKingMated() {
        return isKingMated(true) || isKingMated(false);
    }

    private boolean isKingMated(boolean whiteKing) {
        boolean[][] checkBoard = getCheckBoard(whiteKing);

        String kingPos = findKing(whiteKing);

        int[] kingPosIds = getPosIds(kingPos);

        List<int[]> moves = Piece.getAttackPattern(Piece.convertFromChar(findPiece(kingPos)));
        List<int[]> positions = moves.stream().map(vector ->
            new int[]{
                kingPosIds[0] + vector[0],
                kingPosIds[1] + vector[1],
            }
        ).filter(coord -> coord[0] >= 0 && coord[0] < 8 && coord[1] >= 0 && coord[1] < 8).collect(Collectors.toList());
        positions.add(kingPosIds);

        return positions.stream().allMatch(coord -> checkBoard[coord[0]][coord[1]]);
    }

    private char[][] makeMove(String move, MoveType moveType) {
        char[][] copy = Arrays.copyOf(this.board, this.board.length);

        String currPos = getCurrPos(move);
        String dstPos = getDstPos(move);
        
        int[] currPosIds = getPosIds(currPos);
        int[] dstPosIds = getPosIds(dstPos);

        char piece = findPiece(currPos);

        if (moveType.equals(MoveType.REGULAR) || moveType.equals(MoveType.PAWN_TWO_SQUARES)) {            
            copy[currPosIds[0]][currPosIds[1]] = '\0';
            copy[dstPosIds[0]][dstPosIds[1]] = piece;

            if (moveType.equals(MoveType.PAWN_TWO_SQUARES)) {
                this.enPassant = dstPos;
            }
            
            if (Piece.convertFromChar(piece).isRook()) {
                for (Object key : this.castlingRights.keySet()) {
                    String rookPos = this.castlingRights.getProperty(key.toString());
                    if (rookPos.startsWith(currPos)) {
                        this.castlingRights.remove(key);
                        break;
                    }
                }
            }
        }

        if (moveType.equals(MoveType.CASTLING)) {
            String rookMove = this.castlingRights.getProperty(move);
            copy = makeMove(rookMove, MoveType.REGULAR);

            copy[currPosIds[0]][currPosIds[1]] = '\0';
            copy[dstPosIds[0]][dstPosIds[1]] = piece;

            this.castlingRights.remove(move);
        }

        if (moveType.equals(MoveType.PROMOTION)) {
            char promoteTo = move.charAt(move.length() - 1);
            promoteTo = whiteToMove ? Character.toUpperCase(promoteTo) : promoteTo;

            copy[currPosIds[0]][currPosIds[1]] = '\0';
            copy[dstPosIds[0]][dstPosIds[1]] = promoteTo;
        }

        if (moveType.equals(MoveType.EN_PASSANT)) {            
            int[] enPassantIds = getPosIds(this.enPassant);

            copy[currPosIds[0]][currPosIds[1]] = '\0';
            copy[dstPosIds[0]][dstPosIds[1]] = piece;
            copy[enPassantIds[0]][enPassantIds[1]] = piece;
        }

        if (!moveType.equals(MoveType.PAWN_TWO_SQUARES)) {
            this.enPassant = "";
        }

        return copy;
    }

    public void updateChessboard(String move) throws IllegalMoveException {
        if (this.isMate) {
            throw new IllegalMoveException("Game over"); 
        }

        if (validateMove(move)) {
            char[][] oldBoard = this.board;

            this.board = makeMove(move, this.moveType);

            if (isKingInCheck()) {
                this.isMate = isKingMated();
                this.board = oldBoard;

                throw new IllegalMoveException("King is in check");
            }

            this.whiteToMove = !this.whiteToMove;
        } else {
            throw new IllegalMoveException("Illegal move " + move);
        }
    }

    public boolean isGameOver() {
        return this.isMate;
    }

    @Override
    public String toString() {
        String chessboard = "";

        chessboard += "--Chessboard--\n";
        for (int i = board.length - 1; i >= 0; i--) {
            chessboard += Arrays.toString(board[i]);
            chessboard += "\n";
        }

        chessboard += "this.whiteTomove=" + this.whiteToMove;
        chessboard += ";this.enPassant=" + this.enPassant;
        chessboard += ";this.castlingRights=" + this.castlingRights;
        chessboard += ";this.isMate=" + this.isMate;
        chessboard += ";this.moveType=" + this.moveType;
        chessboard += ";";

        return chessboard;
    }
}
