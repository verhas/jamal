package javax0.jamal.git;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.*;

import java.io.File;

public class TestTag {

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
    @DisplayName("Get all the tags")
    public void allTags() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:tags orderByDate}")
                .results("v1.0,v2.0,3.0");
    }

    @Test
    @DisplayName("Get all the tags starting with V")
    public void allTagsV() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:tags orderByDate match=\"v.*\"}")
                .results("v1.0,v2.0");
    }

    @Test
    @DisplayName("Get all the tags starting with V special separator")
    public void allTagsVSep() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:tags orderByDate match=\"v.*\" sep=:}")
                .results("v1.0:v2.0");
    }

    @Test
    @DisplayName("Get the latest tag")
    public void latestTag() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:tags last orderByDate}")
                .results("3.0");
    }

    @Test
    @DisplayName("Get the last tag by name")
    public void lastTag() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:tags last orderByName}")
                .results("v2.0");
    }

    @Test
    @DisplayName("Get the last tag by name time")
    public void lastTagtime() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:tags last orderByName time}")
                .results("20");
    }

    @Test
    @DisplayName("Get the last tag by name hashCode")
    public void lastTagHash() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:tags last orderByName hash}")
                .results(s -> s.matches("[0-9a-f]{40}"));
    }


    @Test
    @DisplayName("Get all the branchs")
    public void allBranches() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:branches orderByName}")
                .results("branch1,branch2,branch3,branch4,branch5,main,release");
    }

    @Test
    @DisplayName("Get the last branch by name")
    public void lastBranch() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:branches last orderByName}")
                .results("release");
    }

    @Test
    @DisplayName("Get the last branch by name time")
    public void lastBranchtime() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:branches last orderByName time}")
                .results(s -> s.matches("\\d+"));
    }

    @Test
    @DisplayName("Get the last branch by name hashCode")
    public void lastBranchHash() throws Exception {
        TestThat.theInput("{@git location=\"" + helper.getRepoDir() + "\"}{@git:branches last orderByName hash}")
                .results(s -> s.matches("[0-9a-f]{40}"));
    }

}
