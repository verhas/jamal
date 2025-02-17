package javax0.jamal.tools;

import javax0.jamal.api.Input;
import javax0.jamal.api.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSentinel {
    public static final String SENTINEL_TYPE = "testType";
    @TempDir
    File tempDir;

    private Input input;
    private Path approvalFile;

    @BeforeEach
    void setUp() {
        approvalFile = Path.of(tempDir.getAbsolutePath(), ".testType.sentinel");
        this.input = new javax0.jamal.tools.Input(new Position(tempDir.getAbsolutePath() + "/t.jam"));
    }

    @Test
    void testApprovalFileExistsAndValid() throws Exception {
        // Manually create the approval file
        Files.createFile(approvalFile);
        setReadOnlyPermissions(approvalFile);

        Assertions.assertTrue(Sentinel.forThe(input).withType(SENTINEL_TYPE).check());
    }

    @Test
    void testApprovalFileMissing() {
        // Ensure the approval file does not exist
        assertFalse(Files.exists(approvalFile));

        final var sentinel = Sentinel.forThe(input).withType(SENTINEL_TYPE);
        assertFalse(sentinel.check());
        assertTrue(sentinel.getErrorMessage().contains("Approval file does not exist"));
    }

    @Test
    void testApprovalFileWithWrongPermissions() throws Exception {
        Files.createFile(approvalFile);

        if (isWindows()) {
            // On Windows, setting POSIX permissions is not possible.
            // Instead, clear the read-only attribute.
            Files.getFileAttributeView(approvalFile, DosFileAttributeView.class).setReadOnly(false);
        } else {
            // On Linux/macOS, set POSIX permissions
            Files.setPosixFilePermissions(approvalFile, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        }

        final var sentinel = Sentinel.forThe(input).withType(SENTINEL_TYPE);
        assertFalse(sentinel.check());
        assertTrue(sentinel.getErrorMessage().contains("Approval file permissions are incorrect"));
    }

    @Test
    void testFixingPermissionsMakesApprovalValid() throws Exception {
        Files.createFile(approvalFile);

        if (isWindows()) {
            Files.getFileAttributeView(approvalFile, DosFileAttributeView.class).setReadOnly(false);
        } else {
            Files.setPosixFilePermissions(approvalFile, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        }

        final var sentinel = Sentinel.forThe(input).withType(SENTINEL_TYPE);

        // Fix permissions
        setReadOnlyPermissions(approvalFile);

        assertFalse(sentinel.check());
        final var sentinel2 = Sentinel.forThe(input).withType(SENTINEL_TYPE);
        assertTrue(sentinel2.check());
    }

    // Utility method to detect Windows OS
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    // Utility method to set correct read-only permissions
    private void setReadOnlyPermissions(Path file) throws IOException {
        if (Files.getFileAttributeView(file, PosixFileAttributeView.class) != null) {
            // POSIX (Linux/macOS)
            Files.setPosixFilePermissions(file, Set.of(PosixFilePermission.OWNER_READ));
        } else {
            // Windows
            Files.getFileAttributeView(file, DosFileAttributeView.class).setReadOnly(true);
        }
    }

}
