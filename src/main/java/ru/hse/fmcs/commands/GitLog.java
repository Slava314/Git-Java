package ru.hse.fmcs.commands;

import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.client.GitCliImpl;
import ru.hse.fmcs.objects.GitCommit;
import ru.hse.fmcs.objects.GitHead;

import java.io.PrintStream;
import java.util.List;

public class GitLog extends GitCommand {
    @Override
    public void run(List<String> arguments) throws GitException {
        String commitHash;
        GitHead head = GitFileUtils.readHeadFromFile();
        PrintStream stream = GitCliImpl.getOutputStream();
        String branch = head.getBranch();
        if (branch.isEmpty()) {
            stream.println("HEAD detached at " + head.getCommitHash());
        } else {
            stream.println("On branch " + branch);
        }
        if (!arguments.isEmpty()) {
            commitHash = arguments.get(0);
        } else {
            commitHash = head.getCommitHash();
        }
        GitCommit commit;
        while (!commitHash.equals(GitCommit.INITIAL_COMMIT)) {
            commit = GitFileUtils.readCommitFromFile(commitHash);
            commit.writeCommitInfo(stream);
            commitHash = commit.getParentHash();
        }
        commit = GitFileUtils.readCommitFromFile(commitHash);
        commit.writeCommitInfo(stream);
    }
}
