package com.mycompany.app.uci;

import java.util.function.Predicate;

public class IsReady extends Command {
    static Predicate<String> predicate = line -> line.contains("readyok");

    public IsReady() {
        super.setPredicate(predicate);
    }
}
