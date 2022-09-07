package ru.hse.fmcs;

import java.io.File;
import java.nio.file.Path;

public final class GitPaths {
    private static Path WORKING_DIRECTORY;
    private static Path GIT_DIRECTORY;
    private static Path OBJECTS;
    private static Path BLOBS;
    private static Path COMMITS;
    private static Path HEAD;
    private static Path INDEX;
    private static Path BRANCH;
    private static Path REFS;
    private static Path HEADS;

    public static void initializePaths(String workingDirectory) {
        WORKING_DIRECTORY = Path.of(workingDirectory);
        GIT_DIRECTORY = Path.of(WORKING_DIRECTORY.toString(), ".gitcli");
        OBJECTS = Path.of(GIT_DIRECTORY.toString(), "objects");
        BLOBS = Path.of(OBJECTS.toString(), "blobs");
        COMMITS = Path.of(OBJECTS.toString(), "commits");
        HEAD = Path.of(GIT_DIRECTORY.toString(), "HEAD");
        INDEX = Path.of(GIT_DIRECTORY.toString(), "INDEX");
        BRANCH = Path.of(GIT_DIRECTORY.toString(), "BRANCH");
        REFS = Path.of(GIT_DIRECTORY.toString(), "refs");
        HEADS = Path.of(REFS.toString(), "heads");
    }

    public static Path getWorkingDirectory() {
        return WORKING_DIRECTORY;
    }

    public static Path getGitDirectory() {
        return GIT_DIRECTORY;
    }

    public static Path getOBJECTS() {
        return OBJECTS;
    }

    public static Path getHEAD() {
        return HEAD;
    }

    public static Path getINDEX() {
        return INDEX;
    }

    public static Path getBRANCH() {
        return BRANCH;
    }

    public static Path getREFS() {
        return REFS;
    }

    public static Path getHEADS() {
        return HEADS;
    }

    public static Path getBLOBS() {
        return BLOBS;
    }

    public static Path getCOMMITS() {
        return COMMITS;
    }
}
