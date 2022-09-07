package ru.hse.fmcs.objects;

import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;

public class GitBranch {
    private String commitHash;
    private String name;

    public GitBranch() { // for Jackson serialization
    }

    public GitBranch(String commitHash, String name) {
        this.commitHash = commitHash;
        this.name = name;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getName() {
        return name;
    }

    public static void makeAndWriteBranch(String commitHash, String name) throws GitException {
        GitBranch branch = new GitBranch(commitHash, name);
        GitFileUtils.writeBranchToFile(branch);
    }
}
