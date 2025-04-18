package javax0.jamal.testsupport;

import javax0.jamal.DocumentConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * SentinelSmith can forge a sentinel file in the root directory of the project.
 * It is necessary to execute tests that use sentinel macros
 */
public class SentinelSmith implements AutoCloseable {

    public SentinelSmith(Path approvalFile) {
        this.approvalFile = approvalFile;
    }

    public static void forge(final String type, final File tempDir) throws Exception {
        final var approvalFile = Path.of(tempDir.getAbsolutePath(), ".python.sentinel");
        createSecureSentinel(approvalFile);
    }

    private final Path approvalFile;

    public static SentinelSmith forge(final String type) throws Exception {
        final var approvalFile = Path.of(DocumentConverter.getRoot(), "." + type + ".sentinel");
        createSecureSentinel(approvalFile);
        return new SentinelSmith(approvalFile);
    }

    @Override
    public void close() throws Exception {
        if (Files.getFileAttributeView(approvalFile, PosixFileAttributeView.class) != null) {
            Files.setPosixFilePermissions(approvalFile, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } else {
            Files.getFileAttributeView(approvalFile, DosFileAttributeView.class).setReadOnly(false);
        }
        Files.delete(approvalFile);
    }


    private static void createSecureSentinel(Path approvalFile) throws IOException {
        if (!Files.exists(approvalFile)) {
            Files.createFile(approvalFile);
        }
        if (Files.getFileAttributeView(approvalFile, PosixFileAttributeView.class) != null) {
            Files.setPosixFilePermissions(approvalFile, Set.of(PosixFilePermission.OWNER_READ));
        } else {
            Files.getFileAttributeView(approvalFile, DosFileAttributeView.class).setReadOnly(true);
        }
    }

}
