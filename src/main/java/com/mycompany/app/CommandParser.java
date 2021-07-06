package com.mycompany.app;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CommandParser {
    private HashMap<String, CommandProps> commands;
    private ArrayList<String> errors;

    CommandParser() throws ParserConfigurationException, SAXException, IOException {
        this.commands = new HashMap<>();
        this.errors = new ArrayList<>();

        this.parseXML();
    }

    private void parseXML() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = docBuilder.parse(getClass().getClassLoader().getResourceAsStream("StockfishCommands.xml"));
        NodeList commands = document.getElementsByTagName("command");
        NodeList errors = document.getElementsByTagName("error");

        for (int i = 0; i < commands.getLength(); i++) {
            Element element = (Element)commands.item(i);
            String name = element.getTextContent();
            String successContains = element.getAttribute("successContains");

            this.commands.put(name, new CommandProps(successContains));
        }

        for (int i = 0; i < errors.getLength(); i++) {
            Element element = (Element)errors.item(i);
            String msg = element.getTextContent();

            this.errors.add(msg);
        }

    }

    public void parseEngineResponse(BufferedReader reader, String command) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        if (!this.commands.containsKey(command)) {
            throw new IllegalArgumentException("Engine command: (" + command + ") not recognized");
        }
        
        CommandProps commandProps = this.commands.get(command);

        while(true && !commandProps.getSuccessMsg().isEmpty()) {
            String line = reader.readLine();

            // check for errors
            if (this.errors.stream().anyMatch(str -> line.contains(str))) {
                System.out.println("Error executing command: " + command);
                break;
            }

            if (line.contains(commandProps.getSuccessMsg())) {
                System.out.println(command + " " + line);
                break; 
            }
        }
    }

    class CommandProps {
        private final String successContains;

        CommandProps(String successContains) {
            this.successContains = successContains;
        }

        public String getSuccessMsg() {
            return this.successContains;
        }
    }
    
}
