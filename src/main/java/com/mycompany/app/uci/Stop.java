package com.mycompany.app.uci;

import java.util.function.Predicate;
import java.util.Optional;
import java.util.function.Function;
import java.util.Arrays;
import java.util.List;

public class Stop extends Command {
    static Predicate<String> predicate = line -> line.contains("bestmove");
    static Function<String, Optional<String>> callback = line -> {
        Optional<String> response = Optional.empty();

        List<String> words = Arrays.asList(line.split(" "));
        int bestMoveIndex = words.indexOf("bestmove");

        if (bestMoveIndex != -1) {
            String bestMove = words.get(words.indexOf("bestmove") + 1);

            response = Optional.of(bestMove);
        }

        return response;
    };

    public Stop() {
        super.setPredicate(predicate);
        super.setCallback(callback);
    }
}
