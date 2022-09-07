package ru.hse.fmcs.objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.hse.fmcs.GitException;
import ru.hse.fmcs.GitFileUtils;
import ru.hse.fmcs.GitHashUtils;

import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.*;

public class GitCommit {
    public static final String INITIAL_COMMIT = "initial";
    private String hash;
    private String message;
    private String parentHash;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
    private Timestamp time;
    private List<String> blobList = new ArrayList<>();

    public String getHash() {
        return hash;
    }

    public String getMessage() { // for Jackson serialization
        return message;
    }

    public String getParentHash() {
        return parentHash;
    }

    public Timestamp getTime() { // for Jackson serialization
        return time;
    }

    public List<String> getBlobList() {
        return blobList;
    }

    public GitCommit() {

    }

    @JsonIgnore
    public String getInfo() {
        return parentHash + String.join("", blobList);
    }

    public GitCommit(List<String> blobs, String message, String parentHash) {
        blobList = blobs;
        this.message = message;
        this.parentHash = parentHash;
        this.time = new Timestamp(System.currentTimeMillis());
        hash = GitHashUtils.getCommitHash(this.getInfo());
    }

    public static void makeAndWriteCommit(String message, GitIndex index) throws GitException {
        GitHead head = GitFileUtils.readHeadFromFile();
        String parentHash = head.getCommitHash();
        HashMap<String, String> addedFiles = index.getAddedFiles();
        HashSet<String> newFiles = index.getNewFiles();
        HashMap<String, String> trackedFiles = index.getTrackedFiles();
        index.getNewFiles().clear();
        List<String> newBlobs = new ArrayList<>();
        GitCommit parentCommit = GitFileUtils.readCommitFromFile(parentHash);
        HashMap<String, String> files = parentCommit.getFilesFromCommit();
        for (String fileName : addedFiles.keySet()) {
            String fileHash = addedFiles.get(fileName);
            files.remove(fileName);
            newBlobs.add(fileHash);
            trackedFiles.put(fileName, fileHash);
            newFiles.remove(fileName);
        }

        newBlobs.addAll(files.values());
        addedFiles.clear();
        GitCommit commit = new GitCommit(newBlobs, message, parentHash);
        GitFileUtils.writeCommitToFile(commit);
        head.setCommitHash(commit.getHash());
        GitFileUtils.writeHeadToFile(head);
        GitFileUtils.writeIndexToFile(index);
        GitBranch.makeAndWriteBranch(commit.getHash(), head.getBranch());
    }

    public static void makeInitialCommit() throws GitException {
        GitCommit commit = new GitCommit(new ArrayList<>(), "", INITIAL_COMMIT);
        commit.setHash(INITIAL_COMMIT);
        GitFileUtils.writeCommitToFile(commit);
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void writeCommitInfo(PrintStream stream) {
        if (hash.equals(INITIAL_COMMIT)) {
            stream.println("Initial commit");
        } else {
            stream.println("Commit: " + hash);
        }
        stream.println("Author: Test user");
        stream.println("Date: " + time);
        if (!message.isEmpty()) {
            stream.println("Message: " + message);
        }
        stream.print('\n');
    }

    public boolean isParentCommit(String otherHash) throws GitException {
        if (otherHash.equals(GitCommit.INITIAL_COMMIT)) {
            return true;
        }
        String currentHash = hash;
        while (!currentHash.equals(GitCommit.INITIAL_COMMIT)) {
            GitCommit commit = GitFileUtils.readCommitFromFile(currentHash);
            if (commit.getHash().equals(otherHash)) {
                return true;
            }
            currentHash = commit.getParentHash();
        }
        return false;
    }

    @JsonIgnore
    public HashMap<String, String> getFilesFromCommit() throws GitException {
        HashMap<String, String> filesFromCommit = new HashMap<>();
        for (String hash : blobList) {
            GitBlob blob = GitFileUtils.readBlobFromFile(hash);
            filesFromCommit.put(blob.getFileName(), hash);
        }
        return filesFromCommit;
    }

    @JsonIgnore
    private String getBlobHashByFileName(String fileName) throws GitException {
        for (String hash : blobList) {
            GitBlob blob = GitFileUtils.readBlobFromFile(hash);
            if (fileName.equals(blob.getFileName())) {
                return blob.getHash();
            }
        }
        return null;
    }

    public static List<String> makeMergeCommit(GitCommit commit, GitCommit otherCommit, String otherBranch) throws GitException {
        List<String> newBlobs = new ArrayList<>();
        List<String> mergeConflicts = new ArrayList<>();
        HashMap<String, String> filesFromCommit = commit.getFilesFromCommit();
        HashMap<String, String> filesFromOtherCommit = otherCommit.getFilesFromCommit();
        for (Map.Entry<String, String> entry : filesFromOtherCommit.entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            if (filesFromCommit.containsKey(fileName)) {
                if (filesFromCommit.get(fileName).equals(fileHash)) {
                    String blobHash = commit.getBlobHashByFileName(fileName);
                    if (blobHash != null) {
                        newBlobs.add(blobHash);
                    }
                } else {
                    mergeConflicts.add(fileName);
                    String fileData = GitBlob.readDataFromBlob(filesFromCommit.get(fileName));
                    String fileOtherData = GitBlob.readDataFromBlob(filesFromOtherCommit.get(fileName));
                    String data = "<<<<<<< HEAD\n" + fileData + "\n=======\n" + fileOtherData + "\n>>>>>>> " + otherBranch + "\n";
                    GitFileUtils.writeData(fileName, data);
                    String newBlobHash = GitHashUtils.getDataHash(data);
                    GitBlob.makeAndWriteBlob(fileName, newBlobHash);
                    newBlobs.add(newBlobHash);
                }
            } else {
                String blobHash = otherCommit.getBlobHashByFileName(fileName);
                if (blobHash != null) {
                    newBlobs.add(blobHash);
                }
            }
        }
        for (Map.Entry<String, String> entry : filesFromCommit.entrySet()) {
            String fileName = entry.getKey();
            if (!filesFromOtherCommit.containsKey(fileName)) {
                String blobHash = commit.getBlobHashByFileName(fileName);
                if (blobHash != null) {
                    newBlobs.add(blobHash);
                }
            }
        }
        GitCommit mergeCommit = new GitCommit(newBlobs, "merge branch " + otherBranch, commit.getHash());
        GitFileUtils.writeCommitToFile(mergeCommit);
        GitIndex index = GitIndex.makeIndexFromCommit(mergeCommit.getHash());
        GitFileUtils.writeIndexToFile(index);
        GitFileUtils.getStateFromCommit(mergeCommit.getHash());
        GitHead head = GitFileUtils.readHeadFromFile();
        head.setCommitHash(mergeCommit.getHash());
        GitFileUtils.writeHeadToFile(head);
        GitBranch.makeAndWriteBranch(mergeCommit.getHash(), head.getBranch());
        return mergeConflicts;
    }
}
