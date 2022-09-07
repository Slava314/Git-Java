package ru.hse.fmcs.objects;

import org.apache.commons.io.FileUtils;
import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.GitPaths;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitBlob {
    private String hash;
    private String fileName;

    public String getHash() {
        return hash;
    }

    public String getFileName() {
        return fileName;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public GitBlob() {
    }

    public static void makeAndWriteBlob(String fileName, String fileHash) throws GitException {
        GitBlob blob = new GitBlob();
        Path file = Paths.get(GitPaths.getWorkingDirectory().toString(), fileName);
        blob.setFileName(fileName);
        String data;
        try {
            data = Files.readString(file);
            blob.setHash(fileHash);
            Path path = Paths.get(GitPaths.getBLOBS().toString(), blob.getHash(), "data");
            FileUtils.writeStringToFile(path.toFile(), data, Charset.defaultCharset());
        } catch (IOException e) {
            throw new GitException("Can't make blob", e);
        }
        GitFileUtils.writeBlobToFile(blob);
    }

    public static String readDataFromBlob(String blobHash) throws GitException {
        Path path = Paths.get(GitPaths.getBLOBS().toString(), blobHash, "data");
        try {
            return FileUtils.readFileToString(path.toFile(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new GitException("Can't read data from blob", e);
        }
    }
}
