package ru.hse.fmcs.commands;

import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.client.GitCliImpl;
import ru.hse.fmcs.objects.GitHead;
import ru.hse.fmcs.objects.GitIndex;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;

public class GitStatus extends GitCommand {
    @Override
    public void run(List<String> arguments) throws GitException {
        GitIndex index = GitFileUtils.readIndexFromFile();
        PrintStream stream = GitCliImpl.getOutputStream();
        index.updateIndex();
        GitHead head = GitFileUtils.readHeadFromFile();
        String branch = head.getBranch();
        if (branch.isEmpty()) {
            stream.println("HEAD detached at " + head.getCommitHash());
        } else {
            stream.println("On branch " + branch);
        }
        boolean upToDate = true;
        Set<String> addedFiles = index.getAddedFiles().keySet();
        Set<String> newFiles = index.getNewFiles();
        if (!addedFiles.isEmpty()) {
            upToDate = false;
            stream.println("Changes to be committed:");
            for (String file : addedFiles) {
                if (newFiles.contains(file)) {
                    stream.println("new: " + file);
                } else {
                    stream.println("modified: " + file);
                }
            }
        }
        Set<String> modifiedFiles = index.getModifiedFiles().keySet();
        Set<String> deletedFiles = index.getDeletedFiles();
        if (!modifiedFiles.isEmpty() || !deletedFiles.isEmpty()) {
            upToDate = false;
            stream.println("Changes not staged for commit:");
            for (String file : modifiedFiles) {
                stream.println("modified: " + file);
            }
            for (String file : deletedFiles) {
                stream.println("deleted: " + file);
            }
        }
        Set<String> untrackedFiles = index.getUntrackedFiles();
        if (!untrackedFiles.isEmpty()) {
            upToDate = false;
            stream.println("Untracked files:");
            for (String file : untrackedFiles) {
                stream.println(file);
            }
        }
        if (upToDate) {
            stream.println("Everything up to date");
        }
    }
}
