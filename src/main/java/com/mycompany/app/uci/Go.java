package com.mycompany.app.uci;

import java.util.function.Predicate;

public class Go extends Command {
    static Predicate<String> predicate = line -> line.contains("currMove");
    boolean readResponse = false;

    public Go() {
        super.setPredicate(predicate);
        super.setReadResponse(readResponse);
    }

}
