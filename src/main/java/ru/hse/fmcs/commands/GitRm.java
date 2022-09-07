package ru.hse.fmcs.commands;

import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.client.GitCliImpl;
import ru.hse.fmcs.objects.GitIndex;

import java.io.PrintStream;
import java.util.List;

public class GitRm extends GitCommand {
    @Override
    public void run(List<String> arguments) throws GitException {
        GitIndex index = GitFileUtils.readIndexFromFile();
        PrintStream stream = GitCliImpl.getOutputStream();
        int goodFiles = 0;
        for (String file : arguments) {
            if (!GitFileUtils.fileExists(file)) {
                stream.println("Path " + file + " did not match any file");
                continue;
            }
            index.removeFile(file);
            goodFiles++;
        }
        if (goodFiles == 0) {
            stream.println("No files to add");
            return;
        }
        GitFileUtils.writeIndexToFile(index);
        stream.println("Rm completed successful");
    }
}
