package ru.hse.fmcs;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.codec.digest.DigestUtils;

public class GitHashUtils {
    public static String getFileHash(String file) throws GitException {
        try {
            return DigestUtils.sha1Hex(file + Files.readString(GitFileUtils.getFilePath(file)));
        } catch (IOException e) {
            throw new GitException("Can't get SHA1 hash of the file " + file, e);
        }
    }

    public static String getCommitHash(String commitInfo) {
        return DigestUtils.sha1Hex(commitInfo);
    }

    public static String getDataHash(String data) {
        return DigestUtils.sha1Hex(data);
    }
}
