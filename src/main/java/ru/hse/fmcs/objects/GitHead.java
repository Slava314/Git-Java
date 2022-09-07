package ru.hse.fmcs.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.hse.fmcs.GitConstants;
import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;

public class GitHead {
    private String commitHash;
    private String branch;

    public GitHead() {
    }

    public GitHead(String commitHash, String branch) {
        this.commitHash = commitHash;
        this.branch = branch;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    @JsonIgnore
    public static String getParentCommit(int n) throws GitException {
        String currentHash = GitFileUtils.readHeadFromFile().getCommitHash();
        for (int i = 0; i < n; i++) {
            if (currentHash.equals(GitCommit.INITIAL_COMMIT)) {
                throw new GitException("No such commit");
            }
            GitCommit commit = GitFileUtils.readCommitFromFile(currentHash);
            currentHash = commit.getParentHash();
        }
        return currentHash;
    }

    public static void makeAndWriteHead() throws GitException {
        GitHead head = new GitHead(GitCommit.INITIAL_COMMIT, GitConstants.MASTER);
        GitFileUtils.writeHeadToFile(head);
    }

}
