package com.mycompany.app.uci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class Command {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Predicate<String> predicate = line -> true;
    Function<String, Optional<String>> callback = line -> Optional.empty();
    boolean readResponse = true;
    String command = this.getClass().getSimpleName().toLowerCase();

    public void setPredicate(Predicate<String> predicate) {
        this.predicate = predicate;
    }

    public void setCallback(Function<String, Optional<String>> callback) {
        this.callback = callback;
    }

    public void setReadResponse(boolean readResponse) {
        this.readResponse = readResponse;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String build() {
        return this.getClass().getSimpleName().toLowerCase();
    };

    public Optional<String> getResponse(BufferedReader reader) throws IOException {
        Optional<String> response = Optional.empty();

        if (readResponse) {
            Future<Boolean> isReady = executor.submit(() -> {
                while(true) {
                    if (reader.ready()) return reader.ready();
                }
            });
    
            try {
                boolean readerReady = isReady.get(5, TimeUnit.SECONDS);
    
                while(readerReady) {
                    String line = reader.readLine();

                    if (predicate.test(line)) {
                        response = callback.apply(line);
                        break;
                    }
                }
    
            } catch (Exception e) {
                throw new IOException("Cannot read from Stockfish process. " + e.getMessage());
            }
        }

        return response;
    };

    public void send(OutputStreamWriter writer) throws IOException {
        try {
            writer.write(command);
            writer.write("\n");
            writer.flush();
        } catch(IOException e) {
            throw new IOException(e);
        }
    }

}
