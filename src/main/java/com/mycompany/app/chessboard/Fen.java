package com.mycompany.app.chessboard;

public class Fen {
    private String fen;

    public Fen(String fen) {    
        this.fen = fen;
    }

    public Fen() {    
        set("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w AHah -");
    }

    public String get() {
        return this.fen;
    }

    public void set(String fen) {
        this.fen = fen;
    }
}
