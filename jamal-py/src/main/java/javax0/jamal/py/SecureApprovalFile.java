package javax0.jamal.py;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class SecureApprovalFile {
    private final Path approvalFile;

    public SecureApprovalFile(File projectRoot) throws NoSuchAlgorithmException {
        if (projectRoot == null || !projectRoot.isDirectory()) {
            throw new IllegalArgumentException("Invalid project root directory: " + projectRoot);
        }

        this.approvalFile = projectRoot.toPath().resolve(generateApprovalFileName(projectRoot));

        if (!Files.exists(approvalFile)) {
            throw new RuntimeException(errorMessage("Approval file missing ", approvalFile));
        }

        if (!hasValidPermissions(approvalFile)) {
            throw new RuntimeException(errorMessage("Approval file permissions are incorrect", approvalFile));
        }
    }

    public static Path getApprovalFilePath(File projectRoot) throws NoSuchAlgorithmException {
        if (projectRoot == null) {
            throw new IllegalArgumentException("Project root cannot be null.");
        }
        return projectRoot.toPath().resolve(generateApprovalFileName(projectRoot));
    }

    private static String generateApprovalFileName(File projectRoot) throws NoSuchAlgorithmException {
        String input = projectRoot.getAbsolutePath();
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            byte b = hashBytes[i];
            hexString.append(String.format("%02x", b));
        }
        return "." + hexString + ".approve";
    }

    private boolean hasValidPermissions(Path file) {
        try {
            if (Files.isReadable(file) && !Files.isWritable(file)) {
                PosixFileAttributeView posixView = Files.getFileAttributeView(file, PosixFileAttributeView.class);
                if (posixView != null) {
                    // POSIX (Linux/macOS) check
                    PosixFileAttributes attrs = posixView.readAttributes();
                    Set<PosixFilePermission> perms = attrs.permissions();
                    return perms.equals(Set.of(PosixFilePermission.OWNER_READ)); // chmod 400
                } else {
                    // Windows check: Ensure the file is read-only
                    DosFileAttributeView dosView = Files.getFileAttributeView(file, DosFileAttributeView.class);
                    if (dosView != null) {
                        DosFileAttributes attrs = dosView.readAttributes();
                        return attrs.isReadOnly() && !attrs.isHidden();
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private String errorMessage(String reason, Path file) {
        return String.format(
                "%s: Approval file required at:\n  %s\nTo create it, run:\n  touch %s\n  chmod 400 %s",
                reason, file, file, file
        );
    }

    public Path getApprovalFile() {
        return approvalFile;
    }
}
