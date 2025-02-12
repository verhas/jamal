package javax0.jamal.py;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SecureApprovalFileTest {
    @TempDir
    File tempDir;

    private Path approvalFile;

    @BeforeEach
    void setUp() throws Exception {
        approvalFile = SecureApprovalFile.getApprovalFilePath(tempDir);
    }

    @Test
    void testApprovalFileExistsAndValid() throws Exception {
        // Manually create the approval file
        Files.createFile(approvalFile);
        setReadOnlyPermissions(approvalFile);

        assertDoesNotThrow(() -> new SecureApprovalFile(tempDir));
    }

    @Test
    void testApprovalFileMissing() {
        // Ensure the approval file does not exist
        assertFalse(Files.exists(approvalFile));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> new SecureApprovalFile(tempDir));
        assertTrue(exception.getMessage().contains("Approval file missing"));
    }

    @Test
    void testApprovalFileWithWrongPermissions() throws Exception {
        // Create the file but do not set proper permissions
        Files.createFile(approvalFile);
        Files.setPosixFilePermissions(approvalFile, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> new SecureApprovalFile(tempDir));
        assertTrue(exception.getMessage().contains("Approval file permissions are incorrect"));
    }

    @Test
    void testFixingPermissionsMakesApprovalValid() throws Exception {
        // Create the approval file with wrong permissions
        Files.createFile(approvalFile);
        Files.setPosixFilePermissions(approvalFile, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));

        // Ensure it fails first
        assertThrows(RuntimeException.class, () -> new SecureApprovalFile(tempDir));

        // Fix permissions
        setReadOnlyPermissions(approvalFile);

        // Ensure it passes now
        assertDoesNotThrow(() -> new SecureApprovalFile(tempDir));
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
