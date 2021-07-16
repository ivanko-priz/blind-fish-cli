package com.mycompany.app;

import java.util.HashMap;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.*;

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

    public CommandParser() throws ParserConfigurationException, SAXException, IOException {
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
            String extractWordAfter = element.getAttribute("extractWordAfter");
            String[] args = element.getAttribute("args").split(";");

            this.commands.put(name, new CommandProps(successContains, extractWordAfter, args));
        }

        for (int i = 0; i < errors.getLength(); i++) {
            Element element = (Element)errors.item(i);
            String msg = element.getTextContent();

            this.errors.add(msg);
        }
    }

    private String stripArgs(String command) {
        Optional<String> strippedCommand = this.commands
            .entrySet()
            .stream()
            .filter(entry -> command.startsWith(entry.getKey()))
            .map(entry -> entry.getKey())
            .sorted((a, b) -> b.length() - a.length())
            .findFirst();

        return strippedCommand.isPresent() ? strippedCommand.get() : "";
    }

    public CommandParserResponse parseEngineResponse(BufferedReader reader, String command) throws InternalError {
        String strippedCommand = this.stripArgs(command);
        CommandParserResponse response = new CommandParserResponse(false);

        if (!this.commands.containsKey(strippedCommand)) {
            throw new IllegalArgumentException("Engine command: (" + command + ") not recognized");
        }
        
        CommandProps commandProps = this.commands.get(strippedCommand);

        try {
            while(true && !commandProps.successContains.isEmpty()) {
                String line = reader.readLine();
                // System.out.println("reading: " + line);
                // check for errors
                if (this.errors.stream().anyMatch(str -> line.contains(str))) {
                    response = new CommandParserResponse(true);
                    // System.out.println("Error response: " + response);
                    break;
                }
    
                if (line.contains(commandProps.successContains)) {
                    if (!commandProps.extractWordAfter.isEmpty()) {
                        String regex = "(" + commandProps.extractWordAfter + "\\s)" + "(\\w+)";
                        Pattern p = Pattern.compile(regex);
                        Matcher m = p.matcher(line);
    
                        response = m.find() ? new CommandParserResponse(m.group(2), false) : new CommandParserResponse(true);
                    } else {
                        response = new CommandParserResponse(false);
                    }
    
                    break;                  
                }
            }
        } catch(IOException e) {
            throw new InternalError("Internal error");
        }

        return response;
    }

    class CommandProps {
        private final String successContains;
        private final String extractWordAfter;
        private final String[] args;

        CommandProps(String successContains, String extractWordAfter, String[] args) {
            this.successContains = successContains;
            this.extractWordAfter = extractWordAfter;
            this.args = args;
        }

        @Override
        public String toString() {
            return "successContans=" + this.successContains
                + "; extractWordAfter=" + this.extractWordAfter
                + "; args" + Arrays.toString(this.args);

        }
    }    
}
