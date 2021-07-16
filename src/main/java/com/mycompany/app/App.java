package com.mycompany.app;

import com.mycompany.app.commands.BlindFishSetup;
import com.mycompany.app.commands.BlindFishStart;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "blind-fish",
    version = "blind-fish 1.0",
    description = "Play blindfold chess against stockfish engine",
    subcommands = {
        BlindFishSetup.class,
        BlindFishStart.class,
    }
)
public class App implements Callable<Integer>
{
    public static void main( String[] args )
    {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }  

    @Override
    public Integer call() {
        System.out.println("Blind-fish. Play blindfold chess against stockfish engine.");
        return 0;
    }
}
