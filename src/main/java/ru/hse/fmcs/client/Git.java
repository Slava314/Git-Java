package ru.hse.fmcs.client;

import ru.hse.fmcs.GitException;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Git {
    public static void main(String[] args) throws GitException {
        PrintStream stream = GitCliImpl.getOutputStream();
        if (args.length < 2) {
            stream.println("Not enough arguments");
            return;
        }
        Path directoryForGit = Path.of(args[0]);
        String command = args[1];
        GitCliImpl gitCli = new GitCliImpl(directoryForGit.toString());
        gitCli.setOutputStream(System.out);
        gitCli.runCommand(command, Arrays.stream(args).skip(2).collect(Collectors.toList()));
    }
}
