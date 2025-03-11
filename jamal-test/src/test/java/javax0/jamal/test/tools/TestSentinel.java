package javax0.jamal.test.tools;

import javax0.jamal.api.Input;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.testsupport.TestThat;
import javax0.jamal.tools.Sentinel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One extra test to see that the sentinels cache the result with the processor local context properly.
 * This test is in this module, because it needs jamal-engine and there is a jamal-engine -> jamal-tool dependency
 * preventing the jamal-tool -> jamal-engine dependency. However, jamal-test can have the jamal-engine and the
 * jamal-tool as a dependency.
 */
public class TestSentinel {

    public static final String SENTINEL_TYPE = "testType";
    @TempDir
    File tempDir;

    private Input input;
    private Path sentinelFile;

    @BeforeEach
    void setUp() {
        sentinelFile = Path.of(tempDir.getAbsolutePath(), ".testType.sentinel");
        this.input = new javax0.jamal.tools.Input(new Position(tempDir.getAbsolutePath() + "/t.jam"));
    }


    @Test
    @DisplayName("After the sentinel was checked it is too late to create the sentinel file, it is cached")
    void testApprovalFileExistsAndValidCached() throws Exception {
        final var processor = TestThat.theInput("").getProcessor();
        // no sentinel file, it fails
        Assertions.assertFalse(Sentinel.forThe(input,processor).withType(SENTINEL_TYPE).check());

        // Manually create the approval file
        Files.createFile(sentinelFile);
        setReadOnlyPermissions(sentinelFile);
        // check comes from the cache
        Assertions.assertFalse(Sentinel.forThe(input,processor).withType(SENTINEL_TYPE).check());
    }

    @Test
    void testApprovalFileExistsAndValid() throws Exception {
        final var processor = TestThat.theInput("").getProcessor();
        // Manually create the approval file
        Files.createFile(sentinelFile);
        setReadOnlyPermissions(sentinelFile);

        Assertions.assertTrue(Sentinel.forThe(input, processor).withType(SENTINEL_TYPE).check());
    }


    @Test
    void testCachedValueIsUsedTheSecondTime() throws Exception {
        final var processor = TestThat.theInput("").getProcessor();
        Assertions.assertFalse(Sentinel.forThe(input, processor).withType(SENTINEL_TYPE).check());

        // Manually create the approval file
        Files.createFile(sentinelFile);
        setReadOnlyPermissions(sentinelFile);

        Assertions.assertFalse(Sentinel.forThe(input, processor).withType(SENTINEL_TYPE).check());
    }

    @Test
    void testApprovalFileMissing() {
        final var processor = TestThat.theInput("").getProcessor();
        // Ensure the approval file does not exist
        assertFalse(Files.exists(sentinelFile));

        final var sentinel = Sentinel.forThe(input, processor).withType(SENTINEL_TYPE);
        assertFalse(sentinel.check());
        assertTrue(sentinel.getErrorMessage().contains("Approval file does not exist"));
    }

    @Test
    void testApprovalFileWithWrongPermissions() throws Exception {
        final var processor = TestThat.theInput("").getProcessor();
        Files.createFile(sentinelFile);

        if (isWindows()) {
            // On Windows, setting POSIX permissions is not possible.
            // Instead, clear the read-only attribute.
            Files.getFileAttributeView(sentinelFile, DosFileAttributeView.class).setReadOnly(false);
        } else {
            // On Linux/macOS, set POSIX permissions
            Files.setPosixFilePermissions(sentinelFile, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        }

        final var sentinel = Sentinel.forThe(input, processor).withType(SENTINEL_TYPE);
        assertFalse(sentinel.check());
        assertTrue(sentinel.getErrorMessage().contains("Approval file permissions are incorrect"));
    }

    @Test
    @DisplayName("Test that fixing the permission fixes the access whith a mock processor that does not cache")
    void testFixingPermissionsMakesApprovalValid() throws Exception {
        final var processor = Mockito.mock(Processor.class);
        Mockito.when(processor.getLocalContext(Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get(); // Invoke the supplier and return the value
                });
        Files.createFile(sentinelFile);

        if (isWindows()) {
            Files.getFileAttributeView(sentinelFile, DosFileAttributeView.class).setReadOnly(false);
        } else {
            Files.setPosixFilePermissions(sentinelFile, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        }

        final var sentinel = Sentinel.forThe(input, processor).withType(SENTINEL_TYPE);

        // Fix permissions
        setReadOnlyPermissions(sentinelFile);

        assertFalse(sentinel.check());
        final var sentinel2 = Sentinel.forThe(input, processor).withType(SENTINEL_TYPE);
        assertTrue(sentinel2.check());
    }

    // Utility method to detect Windows OS
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    // Utility method to set correct read-only permissions
    public static void setReadOnlyPermissions(Path file) throws IOException {
        if (Files.getFileAttributeView(file, PosixFileAttributeView.class) != null) {
            // POSIX (Linux/macOS)
            Files.setPosixFilePermissions(file, Set.of(PosixFilePermission.OWNER_READ));
        } else {
            // Windows
            Files.getFileAttributeView(file, DosFileAttributeView.class).setReadOnly(true);
        }
    }

}



