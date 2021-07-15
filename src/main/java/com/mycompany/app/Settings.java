package com.mycompany.app;

import java.io.Serializable;

public class Settings implements Serializable {
    private static final long serialVersionUID = 1L;

    private String engine;
    private int level;

    public final static String DIR = "generated";
    public final static String FILE = "settings";

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getEngine() {
        return this.engine;
    }

    public int getLevel() {
        return this.level;
    }
}
