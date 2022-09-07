package ru.hse.fmcs.commands;

import ru.hse.fmcs.GitConstants;
import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.GitPaths;
import ru.hse.fmcs.client.GitCliImpl;
import ru.hse.fmcs.objects.*;
import ru.hse.fmcs.objects.GitCommit;


import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;

public class GitInit extends GitCommand {
    @Override
    public void run(List<String> arguments) throws GitException {
        PrintStream stream = GitCliImpl.getOutputStream();
        if (GitFileUtils.fileExists(GitPaths.getGitDirectory().toAbsolutePath().toString())) {
            throw new GitException("Project is already initialized");
        }
        try {
            Files.createDirectories(GitPaths.getGitDirectory());
            Files.createDirectories(GitPaths.getBLOBS());
            Files.createDirectories(GitPaths.getCOMMITS());
            Files.createDirectories(GitPaths.getHEADS());
            if (!Files.exists(GitPaths.getHEAD())) {
                Files.createFile(GitPaths.getHEAD());
            }
            if (!Files.exists(GitPaths.getINDEX())) {
                Files.createFile(GitPaths.getINDEX());
            }
            if (!Files.exists(GitPaths.getINDEX())) {
                Files.createFile(GitPaths.getINDEX());
            }
        } catch (IOException e) {
            throw new GitException(e);
        }
        GitIndex.makeAndWriteIndex();
        GitHead.makeAndWriteHead();
        GitCommit.makeInitialCommit();
        GitBranch.makeAndWriteBranch(GitCommit.INITIAL_COMMIT, GitConstants.MASTER);
        stream.println("Project initialized");
    }
}
