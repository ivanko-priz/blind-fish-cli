package com.mycompany.chessboard;

import static org.junit.Assert.assertEquals;

import com.mycompany.app.chessboard.Fen;

import org.junit.Test;

public class FenTest {
    @Test
    public void shouldReturnStartPositionIfNoParamsPassed() {
        Fen fen = new Fen();
        String startPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w AHah -";

        assertEquals(fen.get(), startPosition);
    }
}
