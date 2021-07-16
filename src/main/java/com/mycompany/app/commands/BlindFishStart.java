package com.mycompany.app.commands;

import com.mycompany.app.Settings;
import com.mycompany.app.SettingsHandler;
import com.mycompany.app.EngineConnector;
import com.mycompany.app.Fen;
import com.mycompany.app.CommandParserResponse;

import java.util.concurrent.Callable;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "start",
    description = "Start playing chess"
)
public class BlindFishStart implements Callable<Integer> {
    private final String QUIT = "q";
    private final String CANNOT_START_STOCKFISH_ENGINE_ERROR = "Cannot start stockfish engine";
    private final String INTERNAL_SERVER_ERROR = "Internal server error";

    private EngineConnector engineConnector;
    private Fen fen;
    private Settings settings;

    BlindFishStart() {
        try {
            this.settings = SettingsHandler.getSettingsFile();
            this.engineConnector = new EngineConnector(this.settings.getEngine());
            this.fen = new Fen();
        } catch (Exception e) {
            System.exit(1);
        }
    }

    private void prepareEngine() {
        boolean isStarted = this.engineConnector.start();

        if (isStarted) {
            if (this.engineConnector.sendCommand("uci").getError()) {
                throw new InternalError(CANNOT_START_STOCKFISH_ENGINE_ERROR);
            }
            this.engineConnector.sendCommand("isready");
            this.engineConnector.sendCommand("ucinewgame");
            this.engineConnector.sendCommand("position startpos");
            System.out.println("The board is ready, play by using long algebraic notation (e.g. e2e4, e1g1 - short castling, e4d5 - capture).");
        } else {
            throw new InternalError(CANNOT_START_STOCKFISH_ENGINE_ERROR);
        }
    }

    private String wrapMove(String move) {
        this.fen.updateFen(move);
        String command = "position fen " + this.fen.get();

        return command;
    }

    private void processUserMoves() throws InternalError {
        try (
            InputStreamReader in = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(in);
        ) {
            String move;
            CommandParserResponse response;

            while(!(move = br.readLine()).equals(this.QUIT)) {
                try {
                    String command = wrapMove(move);
                    this.engineConnector.sendCommand(command);
                    this.engineConnector.sendCommand("go");
                    response = this.engineConnector.sendCommand("stop");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    continue;
                }

                if (!response.getError()) {
                    this.fen.updateFen(response.getResponse());
                    System.out.println(response.getResponse());
                } else {
                    throw new InternalError(INTERNAL_SERVER_ERROR);
                }
            }
        } catch(IOException e) {
            throw new InternalError(INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Integer call() throws Exception {
        prepareEngine();
        processUserMoves();

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BlindFishStart()).execute(args);
        System.exit(exitCode);
    }
}
