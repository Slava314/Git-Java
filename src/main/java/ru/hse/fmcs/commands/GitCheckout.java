package ru.hse.fmcs.commands;

import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.client.GitCliImpl;
import ru.hse.fmcs.objects.*;
import ru.hse.fmcs.objects.GitCommit;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GitCheckout extends GitCommand {
    @Override
    public void run(List<String> arguments) throws GitException {
        PrintStream stream = GitCliImpl.getOutputStream();
        if (arguments.isEmpty()) {
            throw new GitException("Not enough arguments");
        }
        if (arguments.get(0).equals("--")) {
            GitIndex index = GitFileUtils.readIndexFromFile();
            index.updateIndex();
            HashMap<String, String> addedFiles = index.getAddedFiles();
            HashMap<String, String> modifiedFiles = index.getModifiedFiles();
            HashSet<String> untrackedFiles = index.getUntrackedFiles();
            HashSet<String> deletedFiles = index.getDeletedFiles();
            GitHead head = GitFileUtils.readHeadFromFile();
            GitCommit commit = GitFileUtils.readCommitFromFile(head.getCommitHash());
            HashMap<String, String> filesFromCommit = commit.getFilesFromCommit();
            for (int i = 1; i < arguments.size(); i++) {
                String fileName = arguments.get(i);
                if (!untrackedFiles.contains(fileName)) {
                    addedFiles.remove(fileName);
                    deletedFiles.remove(fileName);
                    modifiedFiles.remove(fileName);
                    String hash = filesFromCommit.get(fileName);
                    GitFileUtils.makeFileFromBlob(hash);
                }
            }
        } else {
            String commitHash;
            String branch = "";
            String revision = arguments.get(0);
            if (revision.startsWith("HEAD~")) {
                commitHash = GitHead.getParentCommit(Integer.parseInt(revision.substring(5)));
            } else if (GitFileUtils.commitExists(revision)) {
                commitHash = revision;
            } else if (GitFileUtils.branchExists(revision)) {
                commitHash = GitFileUtils.readBranchFromFile(revision).getCommitHash();
                branch = revision;
            } else {
                throw new GitException("No such revision");
            }
            checkoutToCommit(commitHash, branch);
        }
        stream.println("Checkout completed successful");
    }

    private void checkoutToCommit(String commitHash, String branch) throws GitException {
        GitIndex index = GitIndex.makeIndexFromCommit(commitHash);
        GitFileUtils.writeIndexToFile(index);
        GitFileUtils.getStateFromCommit(commitHash);
        GitHead head = new GitHead(commitHash, branch);
        GitFileUtils.writeHeadToFile(head);
    }
}
