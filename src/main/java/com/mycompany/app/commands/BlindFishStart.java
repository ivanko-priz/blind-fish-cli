package com.mycompany.app.commands;

import com.mycompany.app.Settings;
import com.mycompany.app.SettingsHandler;
import com.mycompany.app.chessboard.Fen;
import com.mycompany.app.exceptions.IllegalMoveException;
import com.mycompany.app.chessboard.Chessboard;
import com.mycompany.app.EngineConnector;
import com.mycompany.app.uci.*;

import java.util.concurrent.Callable;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

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
    private Chessboard chessboard;
    private Settings settings;
    private List<String> moves = new ArrayList<>();

    BlindFishStart() {
        try {
            this.settings = SettingsHandler.getSettingsFile();
            this.engineConnector = new EngineConnector(this.settings.getEngine());
            this.fen = new Fen();
            this.chessboard = new Chessboard(new Fen().get());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void prepareEngine() {
        boolean isStarted = this.engineConnector.start();

        if (isStarted) {
            try {
                engineConnector.sendCommand(new UCI());
                engineConnector.sendCommand(new IsReady());
                engineConnector.sendCommand(new UciNewGame());
            } catch (IOException e) {
               throw new InternalError(CANNOT_START_STOCKFISH_ENGINE_ERROR);
            }

            System.out.println("The board is ready, play by using long algebraic notation (e.g. e2e4, e1g1 - short castling, e4d5 - capture).");
        } else {
            throw new InternalError(CANNOT_START_STOCKFISH_ENGINE_ERROR);
        }
    }

    private void validateUserMove(String move) throws IllegalMoveException {
        chessboard.updateChessboard(move);
        moves.add(move);
    }

    private Optional<String> sendUserMove(Position command) throws IOException {
        Optional<String> response;

        engineConnector.sendCommand(command);
        engineConnector.sendCommand(new Go());

        response = engineConnector.sendCommand(new Stop());

        return response;
    }

    private void processUserMoves() throws InternalError {
        try (
            InputStreamReader in = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(in);
        ) {
            String move;

            while(!(move = br.readLine()).equals(this.QUIT)) {
                try {
                    validateUserMove(move);
                    
                    Position command = new Position(fen.get(), moves);
                    Optional<String> response = sendUserMove(command);
                
                    if (response.isPresent()) {
                        chessboard.updateChessboard(response.get());
                        moves.add(response.get());
                        System.out.println(response.get());
                    } else {
                        System.out.println(response);
                        throw new InternalError(INTERNAL_SERVER_ERROR);
                    }
                } catch (IllegalMoveException e) {
                    System.out.println("Illegal move: " + e.getMessage());
                    continue;
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
