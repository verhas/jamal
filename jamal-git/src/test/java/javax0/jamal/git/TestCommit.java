package javax0.jamal.git;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestCommit {

    private GitTestHelper helper;

    @BeforeEach
    public void setup() throws Exception {
        helper = new GitTestHelper();
        helper.setup();
    }

    @AfterEach
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    @DisplayName("Get the latest commit name")
    public void latestCommit() throws Exception {
        final var what = new String[]{"hash", "author", "date", "commitTime", "message", "shortMessage", "abbreviated", "committer", "parentIds", "treeId"};
        final var result = new String[]{"[0-9a-f]{40}", "Pinco Palino", "\\d+", "\\d+", "Rev: chicken was added", "Rev: chicken was added", "[0-9a-f]{7}", "Peter Muster", "[0-9a-f]{40}", "[0-9a-f]{40}",};
        for (int i = 0; i < what.length; i++) {
            final int j = i;
            final var theInput = "{@git location=\"" + helper.getRepoDir() + "\"}{@git:commit branch=main last " + what[j] + "}";
            TestThat.theInput(theInput)
                    .results(s -> s.matches(result[j]), r-> String.format("For the %s expected: %s, got: %s", theInput,result[j], r));
        }
    }

    @Test
    @DisplayName("Get the footnote from the latest commit")
    public void latestCommiFootnote() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:commit branch=main last footnote=rev}")
                .results("chicken was added\n");
    }

    @Test
    @DisplayName("Get the latest commit message")
    public void latestCommitMessage() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:commit branch=main last message}")
                .results("Rev: chicken was added");
    }

    @Test
    @DisplayName("Get the last but one commit message")
    public void lastButOneCommitMessage() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:commit branch=main index=1 message}")
                .results("Rev: chucken _\nwas added");
    }

    @Test
    @DisplayName("Get the last but one commit footnote")
    public void lastButOneCommitFootnote() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:commit branch=main index=1 footnote=rev}")
                .results("chucken\nwas added\n");
    }

    @Test
    @DisplayName("Get all commit hashes")
    public void allCommitHashes() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:commits branch=main hash}")
                .results(s -> s.matches("(?:[0-9a-f]{40},)*[0-9a-f]{40}"));
    }

    @Test
    @DisplayName("Get limited commit hashes")
    public void limitedCommitHashes() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:commits branch=main hash limit=1}")
                .results(s -> s.matches("[0-9a-f]{40}"));
    }

}
