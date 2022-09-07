package ru.hse.fmcs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import ru.hse.fmcs.objects.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class GitFileUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Path getFilePath(String fileName) {
        return GitPaths.getWorkingDirectory().resolve(fileName);
    }

    public static List<String> getFilesFromWorkingDirectory() throws GitException {
        Path directory = GitPaths.getWorkingDirectory();
        String gitDirectory = GitPaths.getGitDirectory().toFile().getAbsolutePath();

        List<Path> files;
        try {
            files = Files.walk(directory).filter(Files::isRegularFile)
                    .filter(x -> !x.toFile().getAbsolutePath().startsWith(gitDirectory))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new GitException("Can't list files in working directory", e);
        }
        List<String> result = new ArrayList<>();
        for (Path file : files) {
            String relativeFilePath = directory.relativize(file).toString();
            result.add(relativeFilePath);
        }
        return result;
    }

    public static void writeGitClass(File dest, Object gitClass) throws GitException {
        try {
            mapper.writeValue(dest, gitClass);
        } catch (IOException e) {
            throw new GitException("Can't write git class", e);
        }
    }

    public static <T> T readGitClass(File src, Class<T> cls) throws GitException {
        try {
            return mapper.readValue(src, cls);
        } catch (IOException e) {
            throw new GitException("Can't read git class", e);
        }
    }

    public static void writeBlobToFile(GitBlob blob) throws GitException {
        Path path = Paths.get(GitPaths.getBLOBS().toString(), blob.getHash());
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new GitException("Can't write blob to file", e);
        }
        writeGitClass(Paths.get(path.toString(), "info.json").toFile(), blob);
    }

    public static GitBlob readBlobFromFile(String hash) throws GitException {
        Path path = Paths.get(GitPaths.getBLOBS().toString(), hash, "info.json");
        return readGitClass(path.toFile(), GitBlob.class);
    }

    public static void writeCommitToFile(GitCommit commit) throws GitException {
        writeGitClass(
                Paths.get(GitPaths.getCOMMITS().toString(), commit.getHash() + ".json").toFile(), commit);
    }

    public static GitCommit readCommitFromFile(String hash) throws GitException {
        return readGitClass(
                Paths.get(GitPaths.getCOMMITS().toString(), hash + ".json").toFile(), GitCommit.class);
    }

    public static void writeHeadToFile(GitHead head) throws GitException {
        writeGitClass(GitPaths.getHEAD().toFile(), head);
    }

    public static GitHead readHeadFromFile() throws GitException {
        return readGitClass(GitPaths.getHEAD().toFile(), GitHead.class);
    }

    public static void writeIndexToFile(GitIndex index) throws GitException {
        writeGitClass(GitPaths.getINDEX().toFile(), index);
    }

    public static GitIndex readIndexFromFile() throws GitException {
        return readGitClass(GitPaths.getINDEX().toFile(), GitIndex.class);
    }

    public static void writeBranchToFile(GitBranch branch) throws GitException {
        writeGitClass(
                Paths.get(GitPaths.getHEADS().toString(), branch.getName() + ".json").toFile(), branch);
    }

    public static GitBranch readBranchFromFile(String name) throws GitException {
        return readGitClass(
                Paths.get(GitPaths.getHEADS().toString(), name + ".json").toFile(), GitBranch.class);
    }

    public static void makeFileFromBlob(String blobHash) throws GitException {
        GitBlob blob = readBlobFromFile(blobHash);
        writeData(blob.getFileName(), GitBlob.readDataFromBlob(blobHash));
    }

    public static void getStateFromCommit(String commitHash) throws GitException {
        cleanWorkingDirectory();
        GitCommit commit = readCommitFromFile(commitHash);
        List<String> blobs = commit.getBlobList();
        for (String blob : blobs) {
            makeFileFromBlob(blob);
        }
    }

    private static void cleanWorkingDirectory() throws GitException {
        Path workingDirectoryPath = GitPaths.getWorkingDirectory();
        String gitDirectory = GitPaths.getGitDirectory().toString();
        String[] fileNames = workingDirectoryPath.toFile().list();
        if (fileNames == null) {
            throw new GitException("Can't list files in working directory");
        }
        GitIndex index = GitFileUtils.readIndexFromFile();
        Set<String> untrackedFiles = index.getUntrackedFiles();
        for (String fileName : fileNames) {
            if (untrackedFiles.contains(fileName)) {
                continue;
            }
            Path filePath = getFilePath(fileName);
            if (gitDirectory.equals(filePath.toString())) {
                continue;
            }
            try {
                FileUtils.forceDelete(filePath.toFile());
            } catch (IOException e) {
                throw new GitException("Can't delete " + fileName + " in working directory", e);
            }
        }
    }

    public static boolean fileExists(String fileName) {
        return Files.exists(getFilePath(fileName));
    }

    public static boolean commitExists(String commitHash) {
        return Files.exists(Paths.get(GitPaths.getCOMMITS().toString(), commitHash + ".json"));
    }

    public static boolean branchExists(String branch) {
        return Files.exists(Paths.get(GitPaths.getHEADS().toString(), branch + ".json"));
    }

    public static void deleteBranch(String branchName) throws GitException {
        try {
            FileUtils.forceDelete(Paths.get(GitPaths.getHEADS().toString(), branchName + ".json").toFile());
        } catch (IOException e) {
            throw new GitException("Can't delete branch " + branchName, e);
        }
    }

    public static List<String> listBranches() throws GitException {
        try {
            Stream<Path> files = Files.list(GitPaths.getHEADS());
            return files.map(x -> x.toFile().getName()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new GitException("Can't list branches", e);
        }
    }

    public static void writeData(String fileName, String data) throws GitException {
        Path filePath = Paths.get(GitPaths.getWorkingDirectory().toString(), fileName);
        try {
            FileUtils.writeStringToFile(filePath.toFile(), data, Charset.defaultCharset());
        } catch (IOException e) {
            throw new GitException("Can't write data to file " + fileName, e);
        }
    }
}
