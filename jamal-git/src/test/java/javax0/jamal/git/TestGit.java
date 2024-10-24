package javax0.jamal.git;

import javax0.jamal.testsupport.TestThat;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.TimeZone;

public class TestGit {

    private Repository repository;
    private Git git;
    private File repoDir;

    /**
     * Sets up a local Git repository in a temporary directory for testing purposes.
     *
     * <p>This method is executed before each test, initializing a new local Git repository
     * and performing the following operations:
     * <ul>
     *     <li>Creates a temporary directory to act as the Git repository.</li>
     *     <li>Initializes the Git repository within the temporary directory.</li>
     *     <li>Makes an initial commit with a test file named {@code testFile.txt}.</li>
     *     <li>Retrieves the default branch after the first commit,
     *         creates a new branch named "main", switches to the "main" branch, and
     *         deletes the default branch.</li>
     *     <li>Creates several additional branches, and tags.</li>
     * </ul>
     * The repository is local, and no remote repository is set up. After the setup,
     * the test environment will have a Git repository with the specified branches,
     * tags, and an initial commit on the "main" branch.
     *
     * @throws Exception if any I/O or Git operation fails during the setup.
     */
    @BeforeEach
    public void setup() throws Exception {
        // Create a temporary directory for the test repo
        repoDir = Files.createTempDirectory("testRepo").toFile();
        // Initialize the repository
        repository = FileRepositoryBuilder.create(new File(repoDir, ".git"));
        repository.create();
        git = new Git(repository);

        // Make an initial commit
        File testFile = new File(repoDir, "testFile.txt");
        Files.write(testFile.toPath(), "Initial content".getBytes());
        git.add().addFilepattern("testFile.txt").call();
        git.commit().setMessage("Initial commit").call();
        final var currentBranch = git.getRepository().getBranch();
        git.branchCreate().setName("main").call();
        git.checkout().setName("main").call();
        git.branchDelete().setBranchNames(currentBranch).setForce(true).call();

        // Create some branches
        git.branchCreate().setName("branch1").call();
        git.branchCreate().setName("branch2").call();
        git.branchCreate().setName("branch3").call();
        git.branchCreate().setName("branch4").call();
        git.branchCreate().setName("branch5").call();
        git.branchCreate().setName("release").call();

        final var datev1_0 = new Date();
        final var taggerV1_0 = new PersonIdent("John Doe", "john@email.com", new Date(10000), TimeZone.getTimeZone("GMT"));
        final var taggerV2_0 = new PersonIdent("John Doe", "john@email.com", new Date(20000), TimeZone.getTimeZone("GMT"));
        final var taggerV3_0 = new PersonIdent("John Doe", "john@email.com", new Date(30000), TimeZone.getTimeZone("GMT"));

        // Create some tags
        git.tag().setName("v1.0").setMessage("This is the tag v1.0").setTagger(taggerV1_0).call();
        git.tag().setName("v2.0").setMessage("This is the tag v1.0").setTagger(taggerV2_0).call();
        git.tag().setName("3.0").setMessage("This is the tag v1.0").setTagger(taggerV3_0).call();
    }

    /**
     * Cleans up the resources and deletes the temporary Git repository after each test.
     *
     * <p>This method is executed after each test, performing the following operations:
     * <ul>
     *     <li>Closes the Git repository to release any resources associated with it.</li>
     *     <li>Closes the Git instance to free up resources used during Git operations.</li>
     *     <li>Recursively deletes the temporary directory and all its contents
     *         that were created during the setup phase.</li>
     * </ul>
     * The cleanup ensures that the test environment remains isolated,
     * with no leftover data or repository after each test execution.
     *
     * @throws Exception if an error occurs while closing the repository or deleting files.
     */
    @AfterEach
    public void tearDown() throws Exception {
        // Clean up the temporary repository
        git.getRepository().close();
        git.close();
        deleteDirectory(repoDir);
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    Assumptions.assumeTrue(file.delete());
                }
            }
        }
        Assumptions.assumeTrue(directory.delete());
    }

    @Test
    @DisplayName("Get all the tags")
    public void allTags() throws Exception {
        TestThat.theInput("{@git location=\"" + repoDir.getAbsolutePath() + "\"}{@git:tags orderByDate}")
                .results("v1.0,v2.0,3.0");
    }

    @Test
    @DisplayName("Get all the tags starting with V")
    public void allTagsV() throws Exception {
        TestThat.theInput("{@git location=\"" + repoDir.getAbsolutePath() + "\"}{@git:tags orderByDate match=\"v.*\"}")
                .results("v1.0,v2.0");
    }

    @Test
    @DisplayName("Get all the tags starting with V special separator")
    public void allTagsVSep() throws Exception {
        TestThat.theInput("{@git location=\"" + repoDir.getAbsolutePath() + "\"}{@git:tags orderByDate match=\"v.*\" sep=:}")
                .results("v1.0:v2.0");
    }

    @Test
    @DisplayName("Get the latest tag")
    public void latestTag() throws Exception {
        TestThat.theInput("{@git location=\"" + repoDir.getAbsolutePath() + "\"}{@git:tags last orderByDate}")
                .results("3.0");
    }

    @Test
    @DisplayName("Get the last tag by name")
    public void lastTag() throws Exception {
        TestThat.theInput("{@git location=\"" + repoDir.getAbsolutePath() + "\"}{@git:tags last orderByName}")
                .results("v2.0");
    }

    @Test
    @DisplayName("Get the last tag by name time")
    public void lastTagtime() throws Exception {
        TestThat.theInput("{@git location=\"" + repoDir.getAbsolutePath() + "\"}{@git:tags last orderByName time}")
                .results("20");
    }

    @Test
    @DisplayName("Get the last tag by name hashCode")
    public void lastTagHash() throws Exception {
        final var result = TestThat.theInput("{@git location=\"" + repoDir.getAbsolutePath() + "\"}{@git:tags last orderByName hash}")
                .results();
        Assertions.assertTrue(result.matches("[0-9a-f]{40}"));
    }


    @Test
    @DisplayName("Get all the branchs")
    public void allBranches() throws Exception {
        TestThat.theInput("{@git location=\"" + repoDir.getAbsolutePath() + "\"}{@git:branches orderByName}")
                .results("branch1,branch2,branch3,branch4,branch5,main,release");
    }

    @Test
    @DisplayName("Get the last branch by name")
    public void lastBranch() throws Exception {
        TestThat.theInput("{@git location=\"" + repoDir.getAbsolutePath() + "\"}{@git:branches last orderByName}")
                .results("release");
    }

    @Test
    @DisplayName("Get the last branch by name time")
    public void lastBranchtime() throws Exception {
        final var time = TestThat.theInput("{@git location=\"" + repoDir.getAbsolutePath() + "\"}{@git:branches last orderByName time}")
                .results();
        Assertions.assertTrue(time.matches("\\d+"));
    }

    @Test
    @DisplayName("Get the last branch by name hashCode")
    public void lastBranchHash() throws Exception {
        final var result = TestThat.theInput("{@git location=\"" + repoDir.getAbsolutePath() + "\"}{@git:branches last orderByName hash}")
                .results();
        Assertions.assertTrue(result.matches("[0-9a-f]{40}"));
    }

}
