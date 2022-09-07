package ru.hse.fmcs.commands;

import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.client.GitCliImpl;
import ru.hse.fmcs.objects.GitBranch;
import ru.hse.fmcs.objects.GitCommit;
import ru.hse.fmcs.objects.GitHead;
import ru.hse.fmcs.objects.GitIndex;

import java.io.PrintStream;
import java.util.List;

public class GitReset extends GitCommand {
    @Override
    public void run(List<String> arguments) throws GitException {
        PrintStream stream = GitCliImpl.getOutputStream();
        if (arguments.isEmpty()) {
            throw new GitException("Not enough arguments");
        }
        String commitHash = arguments.get(0);
        GitHead head = GitFileUtils.readHeadFromFile();
        GitCommit commit = GitFileUtils.readCommitFromFile(head.getCommitHash());
        if (!commit.isParentCommit(commitHash)) {
            throw new GitException("Wrong commit hash");
        }
        GitIndex index = GitIndex.makeIndexFromCommit(commitHash);
        GitFileUtils.writeIndexToFile(index);
        GitFileUtils.getStateFromCommit(commitHash);
        head.setCommitHash(commitHash);
        GitFileUtils.writeHeadToFile(head);
        GitBranch.makeAndWriteBranch(commitHash, head.getBranch());
        stream.println("Reset successful");
    }
}
