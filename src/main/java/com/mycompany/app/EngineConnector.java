package com.mycompany.app;

import java.lang.Process;
import java.util.Optional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.mycompany.app.uci.*;

public class EngineConnector {
    private Process process;
    private BufferedReader reader;
    private OutputStreamWriter writer;
    private final String path;

    public EngineConnector(String path) throws IOException {
        this.path = path;
    }

    public boolean start() {
        try {
            this.process = new ProcessBuilder(this.path).redirectErrorStream(true).start();
            this.reader = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
            this.writer = new OutputStreamWriter(this.process.getOutputStream());
                        
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<String> sendCommand(Command command) throws IOException {
        command.send(writer);

        Optional<String> response = command.getResponse(reader);

        return response;
    }

    public void stop() throws Exception {
        sendCommand(new Quit());

        this.process.destroy();
    }
}
