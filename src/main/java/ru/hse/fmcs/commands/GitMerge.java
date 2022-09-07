package ru.hse.fmcs.commands;

import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.client.GitCliImpl;
import ru.hse.fmcs.objects.*;
import ru.hse.fmcs.objects.GitCommit;

import java.io.PrintStream;
import java.util.List;

public class GitMerge extends GitCommand {
    @Override
    public void run(List<String> arguments) throws GitException {
        PrintStream stream = GitCliImpl.getOutputStream();
        if (arguments.isEmpty()) {
            throw new GitException("Not enough arguments");
        }
        GitHead head = GitFileUtils.readHeadFromFile();
        if (head.getBranch().isEmpty()) {
            throw new GitException("Head is detached, can't merge");
        }
        String branchName = arguments.get(0);
        if (!GitFileUtils.branchExists(branchName)) {
            throw new GitException("no such branch");
        }
        GitBranch otherBranch = GitFileUtils.readBranchFromFile(branchName);
        GitCommit otherCommit = GitFileUtils.readCommitFromFile(otherBranch.getCommitHash());
        GitCommit commit = GitFileUtils.readCommitFromFile(head.getCommitHash());
        if (commit.isParentCommit(otherCommit.getHash())) {
            throw new GitException("Already up to date");
        }
        if (otherCommit.isParentCommit(commit.getHash())) {
            GitBranch.makeAndWriteBranch(otherCommit.getHash(), head.getBranch());
            stream.println("Fast-forward merge successful");
            head.setCommitHash(otherCommit.getHash());
            GitFileUtils.writeHeadToFile(head);
            GitIndex index = GitIndex.makeIndexFromCommit(otherCommit.getHash());
            GitFileUtils.writeIndexToFile(index);
            GitFileUtils.getStateFromCommit(otherCommit.getHash());
            return;
        }
        List<String> files = GitCommit.makeMergeCommit(commit, otherCommit, branchName);
        if (!files.isEmpty()) {
            stream.println("Merge conflicts in files: " + String.join(", ", files));
        }
        stream.println("Merge successful");
    }
}
