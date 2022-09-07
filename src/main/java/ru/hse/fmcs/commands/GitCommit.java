package ru.hse.fmcs.commands;

import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.client.GitCliImpl;
import ru.hse.fmcs.objects.GitIndex;

import java.io.PrintStream;
import java.util.List;

public class GitCommit extends GitCommand {
    @Override
    public void run(List<String> arguments) throws GitException {
        PrintStream stream = GitCliImpl.getOutputStream();
        String message = "";
        if (!arguments.isEmpty()) {
            message = arguments.get(0);
        }
        GitIndex index = GitFileUtils.readIndexFromFile();
        index.updateIndex();
        if (index.getAddedFiles().isEmpty()) {
            throw new GitException("No files to commit");
        }
        ru.hse.fmcs.objects.GitCommit.makeAndWriteCommit(message, index);
        stream.println("Files committed");
    }
}
