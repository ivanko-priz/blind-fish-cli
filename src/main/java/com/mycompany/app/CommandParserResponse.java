package com.mycompany.app;

public class CommandParserResponse {
    private final String response;
    private final boolean error;

    CommandParserResponse(String response, boolean error) {
        this.response = response;
        this.error = error;
    }

    CommandParserResponse(boolean error) {
        this.response = "";
        this.error = error;
    }

    public String getResponse() {
        return this.response;
    }

    public boolean getError() {
        return this.error;
    }

    @Override
    public String toString() {
        return "response=" + this.response + "; error=" + this.error; 
    }
}
