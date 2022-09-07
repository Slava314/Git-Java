package ru.hse.fmcs.objects;

import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.GitHashUtils;

import java.util.*;

public class GitIndex {

    private HashSet<String> untrackedFiles = new HashSet<>();
    private final HashMap<String, String> addedFiles = new HashMap<>();
    private final HashSet<String> deletedFiles = new HashSet<>();
    private final HashSet<String> newFiles = new HashSet<>();
    private HashMap<String, String> trackedFiles = new HashMap<>();
    private final HashMap<String, String> modifiedFiles = new HashMap<>();

    public HashSet<String> getUntrackedFiles() {
        return untrackedFiles;
    }

    public HashMap<String, String> getAddedFiles() {
        return addedFiles;
    }

    public HashSet<String> getDeletedFiles() {
        return deletedFiles;
    }

    public HashMap<String, String> getTrackedFiles() {
        return trackedFiles;
    }

    public HashMap<String, String> getModifiedFiles() {
        return modifiedFiles;
    }

    public HashSet<String> getNewFiles() {
        return newFiles;
    }

    public void setTrackedFiles(HashMap<String, String> trackedFiles) {
        this.trackedFiles = trackedFiles;
    }

    public void setUntrackedFiles(HashSet<String> untrackedFiles) {
        this.untrackedFiles = untrackedFiles;
    }

    public GitIndex() {
    }

    public static void makeAndWriteIndex() throws GitException {
        GitIndex index = new GitIndex();
        GitFileUtils.writeIndexToFile(index);
    }

    public void addFile(String fileName) throws GitException {
        if (GitFileUtils.fileExists(fileName)) {
            String fileHash = GitHashUtils.getFileHash(fileName);
            String oldHash = trackedFiles.get(fileName);
            if (oldHash == null) {
                trackedFiles.put(fileName, fileHash);
                newFiles.add(fileName);
                addedFiles.put(fileName, fileHash);
                GitBlob.makeAndWriteBlob(fileName, fileHash);
            } else if (!fileHash.equals(oldHash)) {
                addedFiles.put(fileName, fileHash);
                GitBlob.makeAndWriteBlob(fileName, fileHash);
            }
            modifiedFiles.remove(fileName);
            untrackedFiles.remove(fileName);
        }
    }

    public void removeFile(String fileName) {
        if (GitFileUtils.fileExists(fileName)) {
            String oldHash = trackedFiles.get(fileName);
            if (oldHash != null) {
                trackedFiles.remove(fileName);
                addedFiles.remove(fileName);
                modifiedFiles.remove(fileName);
                newFiles.remove(fileName);
                untrackedFiles.add(fileName);
            }
        }
    }

    public void updateIndex() throws GitException {
        untrackedFiles = new HashSet<>();
        List<String> files = GitFileUtils.getFilesFromWorkingDirectory();
        for (String file : files) {
            String oldHash = trackedFiles.get(file);
            if (oldHash == null) {
                untrackedFiles.add(file);
            } else {
                String fileHash = GitHashUtils.getFileHash(file);
                if (!fileHash.equals(oldHash)) {
                    if (!addedFiles.containsKey(file) || !addedFiles.get(file).equals(fileHash)) {
                        modifiedFiles.put(file, fileHash);
                        addedFiles.remove(file);
                        newFiles.remove(file);
                    }
                }
            }
        }
        Set<String> trackedFilesKeys = trackedFiles.keySet();
        for (String file : trackedFilesKeys) {
            if (!GitFileUtils.fileExists(file)) {
                deletedFiles.add(file);
                addedFiles.remove(file);
                modifiedFiles.remove(file);
                newFiles.remove(file);
            }
        }
    }

    public static GitIndex makeIndexFromCommit(String commitHash) throws GitException {
        GitIndex oldIndex = GitFileUtils.readIndexFromFile();
        oldIndex.updateIndex();
        GitIndex index = new GitIndex();
        index.setUntrackedFiles(oldIndex.getUntrackedFiles());
        GitCommit commit = GitFileUtils.readCommitFromFile(commitHash);
        List<String> blobs = commit.getBlobList();
        HashMap<String, String> trackedFiles = new HashMap<>();
        for (String blobHash : blobs) {
            GitBlob blob = GitFileUtils.readBlobFromFile(blobHash);
            trackedFiles.put(blob.getFileName(), blob.getHash());
        }
        index.setTrackedFiles(trackedFiles);
        return index;
    }
}
