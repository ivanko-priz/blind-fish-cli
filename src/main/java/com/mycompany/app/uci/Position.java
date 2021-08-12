package com.mycompany.app.uci;

import java.util.List;

public class Position extends Command {       
    public Position(String fen, List<String> moves) {
        String movesArgs = moves.size() > 0 ? "moves " + moves.stream().reduce("", (a, move) -> a + move + " ") : "";
        String command = build() +  " fen " + fen + " " + movesArgs;

        super.setCommand(command);
        super.setReadResponse(false);
    }
}
