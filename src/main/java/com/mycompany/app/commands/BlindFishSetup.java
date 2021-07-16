package com.mycompany.app.commands;

import com.mycompany.app.Settings;
import com.mycompany.app.SettingsHandler;

import java.io.IOException;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Command;

@Command(
    name = "setup",
    description = "Set up configuration to play with Stockfish"
)
public class BlindFishSetup implements Callable<Integer> {

    @Option(names = { "-e", "--engine" }, description = "Path to Stockfish executable")
    private String engine;

    @Option(names = { "-l", "--level" }, description = "Stockfish mastery level. Accepts values from 0 to 10, default level is 10")
    private int level;

    @Spec
    private CommandSpec spec;

    private void writeSettingsFile() throws IOException, ClassNotFoundException {
        Settings settings;

        if (!SettingsHandler.exists()) {
            SettingsHandler.createSettingsFile();
            
            settings = new Settings();
        } else {
            settings = SettingsHandler.getSettingsFile();
        }

        ParseResult pr = spec.commandLine().getParseResult();
        if (pr.hasMatchedOption("engine")) settings.setEngine(engine);
        if (pr.hasMatchedOption("level")) settings.setLevel(level);

        SettingsHandler.serializeSettings(settings);
    } 

    @Override
    public Integer call() throws Exception {
        this.writeSettingsFile();

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BlindFishSetup()).execute(args);
        System.exit(exitCode);
    }
    
}
