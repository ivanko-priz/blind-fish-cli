package com.mycompany.app;

import java.lang.Process;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class EngineConnector {
    private Process process;
    private BufferedReader reader;
    private OutputStreamWriter writer;
    private final CommandParser commandParser;
    private final String path;

    public EngineConnector(String path) throws ParserConfigurationException, SAXException, IOException {
        this.path = path;
        this.commandParser = new CommandParser();
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

    public CommandParserResponse sendCommand(String command) throws InternalError {              
        CommandParserResponse response = new CommandParserResponse(true);
        
        try {
            this.writer.write(command + "\n");
            this.writer.flush();

            response = this.commandParser.parseEngineResponse(reader, command);
        } catch (IOException e) {
            throw new InternalError("Internal error");
        }

        return response;
    }

    public void stop() throws Exception {
        sendCommand("quit");

        this.process.destroy();
    }
}
