package com.mycompany.app.uci;

import java.util.function.Predicate;

public class UCI extends Command {
    static Predicate<String> predicate = line -> line.contains("uciok");

    public UCI() {
        super.setPredicate(predicate);
    }
}
