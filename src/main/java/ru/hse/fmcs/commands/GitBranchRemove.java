package ru.hse.fmcs.commands;

import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.client.GitCliImpl;
import ru.hse.fmcs.objects.GitHead;

import java.io.PrintStream;
import java.util.List;

public class GitBranchRemove extends GitCommand {
    @Override
    public void run(List<String> arguments) throws GitException {
        PrintStream stream = GitCliImpl.getOutputStream();
        if (arguments.isEmpty()) {
            throw new GitException("Not enough arguments");
        }
        String branchName = arguments.get(0);
        GitHead head = GitFileUtils.readHeadFromFile();
        if (!GitFileUtils.branchExists(branchName)) {
            throw new GitException("Branch not exists");
        }
        GitFileUtils.deleteBranch(branchName);
        if (head.getBranch().equals(branchName)) {
            head.setBranch("");
            GitFileUtils.writeHeadToFile(head);
        }
        stream.println("Branch " + branchName + " removed successfully");
    }
}
