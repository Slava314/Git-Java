package ru.hse.fmcs;


import org.junit.jupiter.api.Test;
import ru.hse.fmcs.client.GitCli;
import ru.hse.fmcs.client.GitCliImpl;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Т.к. в коммитах при каждом новом запуске получаются разные хеши и
 *   разное время отправки, то в expected логах на их местах используются
 *   COMMIT_HASH и COMMIT_DATE заглушки соответственно
 */
public class GitTest extends AbstractGitTest {
    @Override
    protected GitCli createCli(String workingDir) {
        return new GitCliImpl(workingDir);
    }

    @Override
    protected TestMode testMode() {
        return TestMode.SYSTEM_OUT;
    }

    @Test
    public void testAdd() throws Exception {
        init();
        commit("a");
        createFile("file.txt", "aaa");
        status();
        add("file.txt");
        status();
        commit("First commit");
        status();
        log();

        check("add.txt");
    }

    @Test
    public void testAddAndModify() throws Exception {
        String file = "file.txt";
        createFileAndCommit(file, "aaa");
        createFile(file, "bbb");
        add(file);
        status();
        createFile(file, "ccc");
        status();
        add(file);
        status();
        commit("Second commit");
        status();
    }

    @Test
    public void testAddMultipleFiles() throws Exception {
        createFile("a.txt", "aaa");
        createFile("b.txt", "bbb");
        status();
        add("a.txt", "b.txt");
        status();
        commit("First commit");
        status();
        log();
    }

    @Test
    public void testMultipleCommits() throws Exception {
        String file1 = "a.txt";
        String file2 = "b.txt";
        createFile(file1, "aaa");
        createFile(file2, "bbb");
        status();
        add(file1);
        add(file2);
        status();
        rm(file2);
        status();
        assertTrue(fileExists(file2));
        commit("Add file1.txt");
        add(file2);
        status();
        commit("Add file2.txt");
        status();
        log();

        check("multipleCommits.txt");
    }

    @Test
    public void testCheckoutFile() throws Exception {
        String file = "file.txt";
        createFileAndCommit(file, "aaa", "Add file.txt");

        deleteFile(file);
        status();
        checkoutFiles("--", file);
        assertTrue(fileExists(file));
        checkFileContent(file, "aaa");
        status();

        createFile(file, "bbb");
        checkFileContent(file, "bbb");
        status();
        checkoutFiles("--", file);
        checkFileContent(file, "aaa");
        status();

        check("checkoutFile.txt");
    }

    @Test
    public void testReset() throws Exception {
        String file = "file.txt";
        createFileAndCommit(file, "aaa", "First commit");
        createFileAndCommit(file, "bbb", "Second commit");
        log();

        reset(1);
        checkFileContent(file, "aaa");
        log();

        createFileAndCommit(file, "ccc", "Third commit");
        checkFileContent(file, "ccc");
        log();

        check("reset.txt");
    }

    @Test
    public void testResetWithUntracked() throws Exception {
        String file = "file.txt";
        createFileAndCommit(file, "aaa", "First commit");
        createFileAndCommit(file, "bbb", "Second commit");
        status();
        log();

        String newFile = "new_file.txt";
        createFile(newFile, "ccc");
        status();
        reset(1);
        assertTrue(fileExists(newFile));
        checkFileContent(file, "aaa");
        checkFileContent(newFile, "ccc");
        status();
        log();
    }

    @Test
    public void testResetToOtherBranch() throws Exception {
        String file = "file.txt";
        createFileAndCommit(file, "aaa", "First commit");

        createBranch("develop");
        createFileAndCommit(file, "bbb", "Second commit");
        log();

        checkoutBranch("develop");
        createFileAndCommit(file, "ccc", "Third commit");
        reset("7517197020f4632c2f6f380aade7e68f78c73acb");
    }

    @Test
    public void testCheckout() throws Exception {
        String file = "file.txt";
        createFileAndCommit(file, "aaa", "First commit");
        createFileAndCommit(file, "bbb", "Second commit");
        log();

        checkoutRevision(1);
        checkFileContent(file, "aaa");
        status();
        log();

        checkoutMaster();
        checkFileContent(file, "bbb");
        status();
        log();

        check("checkout.txt");
    }

    @Test
    public void testCheckoutWithModified() throws Exception {
        String file = "file.txt";
        createFileAndCommit(file, "aaa", "First commit");
        createFileAndCommit(file, "bbb", "Second commit");
        log();

        checkFileContent(file, "bbb");
        createFile(file, "ccc");
        checkFileContent(file, "ccc");
        checkoutRevision(1);
        checkFileContent(file, "aaa");
        checkoutMaster();
        checkFileContent(file, "bbb");
    }

    @Test
    public void testCheckoutWithUntracked() throws Exception {
        String file = "file.txt";
        createFileAndCommit(file, "aaa", "First commit");
        createFileAndCommit(file, "bbb", "Second commit");
        status();
        log();

        String newFile = "new_file.txt";
        createFile(newFile, "ccc");
        status();
        checkoutRevision(1);
        assertTrue(fileExists(newFile));
        checkFileContent(file, "aaa");
        checkFileContent(newFile, "ccc");
        status();
        log();
    }

    @Test
    public void testBranches() throws Exception {
        createFileAndCommit("a.txt", "aaa");

        createBranch("develop");
        checkoutBranch("develop");
        createFileAndCommit("b.txt", "bbb");
        status();
        log();
        showBranches();

        checkoutMaster();
        checkFileContent("a.txt", "aaa");
        assertFalse(fileExists("b.txt"));
        status();
        log();

        createBranch("new-feature");
        checkoutBranch("new-feature");
        createFileAndCommit("c.txt", "ccc");
        status();
        log();

        checkoutBranch("develop");
        assertTrue(fileExists("b.txt"));
        checkFileContent("b.txt", "bbb");
        assertFalse(fileExists("c.txt"));
        status();
        log();

        check("branches.txt");
    }

    @Test
    public void testBranchRemove() throws Exception {
        createFileAndCommit("a.txt", "aaa");
        createBranch("develop");
        checkoutBranch("develop");
        createFileAndCommit("b.txt", "bbb");
        status();
        showBranches();
        checkoutBranch("master");
        assertFalse(fileExists("b.txt"));
        status();
        removeBranch("develop");
        showBranches();

        check("branchRemove.txt");
    }

    @Test
    public void testParentMerge() throws Exception {
        createFileAndCommit("a.txt", "aaa");
        createFileAndCommit("b.txt", "bbb");
        createBranch("develop");
        createFileAndCommit("c.txt", "ccc");
        log();
        merge("develop");
        log();
    }

    @Test
    public void testFastForwardMerge() throws Exception {
        createFileAndCommit("a.txt", "aaa");
        createFileAndCommit("b.txt", "bbb");
        createBranch("develop");
        checkoutBranch("develop");
        createFileAndCommit("c.txt", "ccc");
        log();
        checkoutBranch("master");
        assertFalse(fileExists("c.txt"));
        log();
        merge("develop");
        assertTrue(fileExists("c.txt"));
        log();
    }

    @Test
    public void testMerge() throws Exception {
        createFileAndCommit("a.txt", "aaa");
        createFileAndCommit("b.txt", "bbb");
        log();
        createBranch("develop");
        checkoutBranch("develop");
        log();
        createFileAndCommit("c.txt", "ccc");
        createFileAndCommit("d.txt", "ddd");
        log();
        checkoutMaster();
        assertFalse(fileExists("c.txt"));
        assertFalse(fileExists("d.txt"));
        log();
        createBranch("feature");
        checkoutBranch("feature");
        createFileAndCommit("e.txt", "eee");
        assertFalse(fileExists("c.txt"));
        assertFalse(fileExists("d.txt"));
        log();
        merge("develop");
        assertTrue(fileExists("c.txt"));
        assertTrue(fileExists("d.txt"));
        assertTrue(fileExists("e.txt"));
        log();
    }

    @Test
    public void testMergeConflict() throws Exception {
        createFileAndCommit("a.txt", "aaa");

        createBranch("develop");
        createFileAndCommit("b.txt", "bbb");
        checkoutBranch("develop");
        createFileAndCommit("a.txt", "bbb");

        checkoutMaster();
        merge("develop");
        assertTrue(fileExists("a.txt"));
        String expected = "<<<<<<< HEAD\n" +
                "aaa\n=======\nbbb\n" +
                ">>>>>>> develop\n";
        checkFileContent("a.txt", expected);
    }
}

