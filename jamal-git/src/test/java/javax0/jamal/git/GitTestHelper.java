package javax0.jamal.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Assumptions;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.TimeZone;

public class GitTestHelper {

    private Repository repository;
    private Git git;
    private File repoDir;


    public File getRepoDir() {
        return repoDir;
    }

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
    void setup() throws Exception {
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
        git.add().addFilepattern("chuckFile.txt").call();
        git.commit().setMessage("Rev: chucken _\nwas added").call();
        git.add().addFilepattern("chickFile.txt").call();
        git.commit().setMessage("Rev: chicken was added").call();
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
    void tearDown() throws Exception {
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

}
