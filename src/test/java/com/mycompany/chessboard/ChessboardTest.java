package com.mycompany.chessboard;

import com.mycompany.app.chessboard.Chessboard;
import com.mycompany.app.exceptions.IllegalMoveException;

import com.mycompany.chessboard.categories.ExceptionHandling;
import com.mycompany.chessboard.categories.ChessLogic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
// import static org.hamcrest.core.;


import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ChessboardTest {
    @Test
    public void shouldInitWithoutError() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w AHah -";
        
        new Chessboard(fen);
    }

    @Category(ExceptionHandling.class)
    @Test
    public void shouldThrowIfWrongMoveSignatureProvided() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w AHah -";

        Chessboard board = new Chessboard(fen);
        String[] wrongMoves = new String[]{
            "a0b3", "c14b13", "hell_no", ">?DSAq", "1234"
        };

        for (String move : wrongMoves) {
            try {
                board.updateChessboard(move);
            } catch (IllegalMoveException e) {
                assertThat(e.getMessage(), is("Move not recognized"));
            }
        }
    }

    @Category(ExceptionHandling.class)
    @Test 
    public void shouldThrowIfFenPositionContainsMate() {
        String fen = "3k2R1/7R/8/8/8/3K4/8/8 b - - 0 1";

        Chessboard board = new Chessboard(fen);

        assertThat(board.isGameOver(), is(true));
        
        try {
            board.updateChessboard("d8d7");
        } catch (IllegalMoveException e) {
            assertThat(e.getMessage(), is("Game over"));
        }
    }

    @Category(ExceptionHandling.class)
    @Test
    public void shouldThrowIfCurrentPositionIsEmptySquare() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w AHah -";

        Chessboard board = new Chessboard(fen);

        String[] emptySquaresMoves = new String[]{
            "e3e5",
            "b5e3",
            "c4d7",
            "a6h6"
        };

        for (String move : emptySquaresMoves) {
            try {
                board.updateChessboard(move);
            } catch (IllegalMoveException e) {
                assertThat(e.getMessage(), is("Square " + move.substring(0, 2) + " is empty"));
            }
        }
    }

    @Category(ExceptionHandling.class)
    @Test
    public void shouldThrowIfPieceOfWrongColorMoves() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w AHah -";
        Chessboard board = new Chessboard(fen);

        try {
            board.updateChessboard("e7e5");
        } catch(IllegalMoveException e) {
            assertThat(e.getMessage(), containsString("Not your turn."));
        }
        
        // Now fen contains the same position but it's black's first move as you can see by 'b' symbol 
        fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b AHah -";
        board = new Chessboard(fen);

        try {
            board.updateChessboard("d2d4");
        } catch(IllegalMoveException e) {
            assertThat(e.getMessage(), containsString("Not your turn."));
        }
    }

    @Category(ChessLogic.class)
    @Test
    public void shouldAllowPawnMoveTwoSquaresForward() throws IllegalMoveException {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w AHah -";
        Chessboard board = new Chessboard(fen);

        board.updateChessboard("e2e4");

        assertThat(board.getSideToMove(), is(false));
        assertThat(board.getEnPassant(), is("e4"));
        assertThat(board.getBoard()[3][4], is('P'));
        assertThat(board.getBoard()[1][4], is('\0'));
    }

    @Category(ChessLogic.class)
    @Test
    public void shouldAllowNightMoveFromInitPosition() throws IllegalMoveException {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w AHah -";
        Chessboard board = new Chessboard(fen);

        board.updateChessboard("g1f3");

        assertThat(board.getSideToMove(), is(false));
        assertThat(board.getBoard()[2][5], is('N'));
        assertThat(board.getBoard()[0][6], is('\0'));
    }

    @Category(ChessLogic.class)
    @Test
    public void shouldAllowPawnToMoveOneSquare() throws IllegalMoveException {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w AHah -";
        Chessboard board = new Chessboard(fen);

        board.updateChessboard("a2a3");

        assertThat(board.getSideToMove(), is(false));
        assertThat(board.getBoard()[2][0], is('P'));
        assertThat(board.getBoard()[1][0], is('\0'));
    }

    @Category(ChessLogic.class)
    @Test
    public void shouldShortCastleKingIfPossible() throws IllegalMoveException {
        String fen = "rnbqkbnr/pppppppp/8/8/8/3BPN2/PPPP1PPP/RNBQK2R w KQkq - 0 1";
        Chessboard board = new Chessboard(fen);
        String move = "e1g1";

        board.updateChessboard(move);
    
        assertThat("It's now black's move", board.getSideToMove(), is(false));
        assertThat("King is castled to safety", board.getBoard()[0][6], is('K'));
        assertThat("Rook protects king of f1", board.getBoard()[0][5], is('R'));
        assertThat("There's no rook on h8", board.getBoard()[0][7], is('\0'));
        assertThat("There's no king on e1", board.getBoard()[0][4], is('\0'));
        assertThat("White cannot castle kingside", board.getCastlingRights(), not(hasProperty(move)));
        assertThat("White cannot castle queenside",  board.getCastlingRights(), not(hasProperty("e1c1")));
    }

    @Category(ChessLogic.class)
    @Test
    public void shouldLongCastleKingIfPossible() throws IllegalMoveException {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/R3KBNR w KQkq - 0 1";

        Chessboard board = new Chessboard(fen);
        String move = "e1c1";

        board.updateChessboard(move);
    
        assertThat("It's now black's move", board.getSideToMove(), is(false));
        assertThat("King is castled to safety", board.getBoard()[0][2], is('K'));
        assertThat("Rook protects king of d1", board.getBoard()[0][3], is('R'));
        assertThat("There's no rook on h1", board.getBoard()[0][0], is('\0'));
        assertThat("There's no king on e1", board.getBoard()[0][4], is('\0'));
        assertThat("White cannot castle kingside", board.getCastlingRights(), not(hasProperty("e1g1")));
        assertThat("White cannot castle queenside",  board.getCastlingRights(), not(hasProperty(move)));
    }
}
