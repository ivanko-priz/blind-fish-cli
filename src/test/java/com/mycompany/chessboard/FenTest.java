package com.mycompany.chessboard;

import static org.junit.Assert.assertEquals;

import com.mycompany.app.chessboard.Fen;

import org.junit.Test;

public class FenTest {
    @Test
    public void shouldThrowWhenInvalidInputIsPassed() {
        Fen fen = new Fen();
        String invalidMoves[] = {"f4a0", "hello", "0000", "e10a5", "c3", "a1b8q"};

        for (String move : invalidMoves) {
            try {
                fen.updateFen(move);
            } catch (Exception e) {
                assertEquals("Wrong signature for move " + move, e.getMessage());
            }
        }
    }

    @Test
    public void shouldThrowWhenMoveStartsWithEmptySquare() {
        Fen fen = new Fen();

        String invalidMoves[] = {"f4f5", "e3e5", "h6e3"};

        for (String move : invalidMoves) {
            try {
                fen.updateFen(move);
            } catch (Exception e) {
                assertEquals("No piece at given coordinates " + move, e.getMessage());
            }
        }
    }

    @Test
    public void shouldThrowWhenWrongSidePlaysMove() {
        Fen fen = new Fen();

        String invalidMovesBlack[] = {"e7e5", "f8e6"};
        for (String move : invalidMovesBlack) {
            try {
                fen.updateFen(move);
            } catch (Exception e) {
                assertEquals("Not your turn", e.getMessage());
            }
        }

        fen.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b AHah -");
        String invalidMovesWhite[] = {"e2e4", "c1a3"};
        for (String move : invalidMovesWhite) {
            try {
                fen.updateFen(move);
            } catch (Exception e) {
                assertEquals("Not your turn", e.getMessage());
            }
        }
    }

    @Test
    public void shouldAllowPawnToMoveOneSquareForward() throws Exception {
        Fen fen = new Fen();
        
        String moves[] = {"a2a3", "e7e6"};
        String expected[] = {
            "rnbqkbnr/pppppppp/8/8/8/P7/1PPPPPPP/RNBQKBNR b AHah -",
            "rnbqkbnr/pppp1ppp/4p3/8/8/P7/1PPPPPPP/RNBQKBNR w AHah -"
        };

        for (int i = 0; i < moves.length; i++) {
            fen.updateFen(moves[i]);
            assertEquals(expected[i], fen.get());
        }
    }
}
