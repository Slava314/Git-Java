package ru.hse.fmcs.commands;

import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.client.GitCliImpl;

import java.io.PrintStream;
import java.util.List;

public class GitShowBranches extends GitCommand {
    @Override
    public void run(List<String> arguments) throws GitException {
        PrintStream stream = GitCliImpl.getOutputStream();
        stream.println("Available branches:");
        List<String> files = GitFileUtils.listBranches();
        for (String file : files) {
            stream.println(file.substring(0, file.length() - 5));
        }
    }
}
