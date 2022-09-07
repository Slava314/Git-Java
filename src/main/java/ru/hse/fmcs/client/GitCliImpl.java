package ru.hse.fmcs.client;

import org.jetbrains.annotations.NotNull;
import ru.hse.fmcs.GitConstants;
import ru.hse.fmcs.GitException;
import ru.hse.fmcs.commands.*;
import ru.hse.fmcs.commands.GitCommit;
import ru.hse.fmcs.objects.GitHead;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.hse.fmcs.GitPaths.initializePaths;

public class GitCliImpl implements GitCli {
    private static final Map<String, GitCommand> commands = new HashMap<>();

    public static PrintStream getOutputStream() {
        return outputStream;
    }

    private static PrintStream outputStream = System.out;

    static {
        commands.put(GitConstants.INIT, new GitInit());
        commands.put(GitConstants.ADD, new GitAdd());
        commands.put(GitConstants.RM, new GitRm());
        commands.put(GitConstants.STATUS, new GitStatus());
        commands.put(GitConstants.COMMIT, new GitCommit());
        commands.put(GitConstants.RESET, new GitReset());
        commands.put(GitConstants.LOG, new GitLog());
        commands.put(GitConstants.CHECKOUT, new GitCheckout());
        commands.put(GitConstants.BRANCH_CREATE, new GitBranchCreate());
        commands.put(GitConstants.BRANCH_REMOVE, new GitBranchRemove());
        commands.put(GitConstants.SHOW_BRANCHES, new GitShowBranches());
        commands.put(GitConstants.MERGE, new GitMerge());
    }

    public GitCliImpl(String directory) {
        initializePaths(directory);
    }

    @Override
    public void runCommand(@NotNull String command, @NotNull List<@NotNull String> arguments) throws GitException {
        GitCommand gitCommand = commands.get(command);
        if (gitCommand == null) {
            outputStream.println("Unsupported operation");
            return;
        }
        try {
            gitCommand.run(arguments);
        } catch (GitException e) {
            outputStream.println(e.getMessage());
        }
    }

    @Override
    public void setOutputStream(@NotNull PrintStream outputStream) {
        GitCliImpl.outputStream = outputStream;
    }

    @Override
    public @NotNull String getRelativeRevisionFromHead(int n) throws GitException {
        String commitHash = GitHead.getParentCommit(n);
        if (commitHash == null) {
            throw new GitException("Can't get revision");
        }
        return commitHash;
    }

}
