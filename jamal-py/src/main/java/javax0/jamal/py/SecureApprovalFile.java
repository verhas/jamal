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

/**
 * Represents a secure approval file used for validation within a project.
 * This class ensures that the approval file exists and has the correct permissions.
 */
public class SecureApprovalFile {
    private final Path approvalFile;

    /**
     * Constructs a SecureApprovalFile instance, ensuring the approval file exists
     * and has the correct permissions.
     *
     * @param directories The root directory of the project.
     * @throws NoSuchAlgorithmException If the SHA-512 algorithm is not available.
     * @throws IllegalArgumentException If the provided project root is null or not a directory.
     * @throws RuntimeException         If the approval file is missing or has incorrect permissions.
     */
    public SecureApprovalFile(File... directories) throws NoSuchAlgorithmException {
        this.approvalFile = get(directories);
    }


    public Path get(File... directories) throws NoSuchAlgorithmException {
        final Path[] approvalFiles = new Path[directories.length];
        int i = 0;
        for (var projectRoot : directories) {
            if (projectRoot == null || !projectRoot.isDirectory()) {
                continue;
            }

            final var approvalFile = projectRoot.toPath().resolve(generateApprovalFileName(projectRoot));
            approvalFiles[i++] = approvalFile;
            if (!Files.exists(approvalFile)) {
                continue;
            }

            if (!hasValidPermissions(approvalFile)) {
                throw new RuntimeException(errorMessage("Approval file permissions are incorrect", approvalFile));
            }
            return approvalFile;
        }
        throw new RuntimeException(errorMessage("Approval file does not exist", approvalFiles));
    }

    /**
     * Gets the path of the approval file for the given project root.
     *
     * @param projectRoot The root directory of the project.
     * @return The path to the approval file.
     * @throws NoSuchAlgorithmException If the SHA-512 algorithm is not available.
     * @throws IllegalArgumentException If the project root is null.
     */
    public static Path getApprovalFilePath(File projectRoot) throws NoSuchAlgorithmException {
        if (projectRoot == null) {
            throw new IllegalArgumentException("Project root cannot be null.");
        }
        return projectRoot.toPath().resolve(generateApprovalFileName(projectRoot));
    }

    /**
     * Generates the approval file name based on the project root path using SHA-512 hashing.
     *
     * @param projectRoot The root directory of the project.
     * @return The generated approval file name.
     * @throws NoSuchAlgorithmException If the SHA-512 algorithm is not available.
     */
    private static String generateApprovalFileName(File projectRoot) throws NoSuchAlgorithmException {
        String input = projectRoot.getAbsolutePath();
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            byte b = hashBytes[i];
            hexString.append(String.format("%02x", b));
        }
        return "." + hexString + ".approve.python";
    }

    /**
     * Checks if the approval file has valid permissions.
     * Valid permissions are:
     * - Read-only for the owner (chmod 400 on POSIX systems)
     * - Read-only and not hidden on Windows
     *
     * @param file The approval file path.
     * @return {@code true} if the file has valid permissions, {@code false} otherwise.
     */
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

    /**
     * Constructs an error message for missing or incorrect approval file permissions.
     *
     * @param reason The reason for the error.
     * @param files  The file path.
     * @return The formatted error message.
     */
    private String errorMessage(String reason, Path... files) {
        final var sb = new StringBuilder();
        sb.append(String.format("%s: Approval file required at:\n", reason));
        for (Path file : files) {
            final var f = getRealPath(file).getAbsolutePath();
            sb.append(String.format("   touch %s; chmod 400 %s\n            or\n", f, f));
        }
        return sb.toString();
    }

    private File getRealPath(Path file) {
        try {
            return file.toFile().getCanonicalFile();
        } catch (IOException e) {
            return file.toFile();
        }
    }

    /**
     * Returns the path to the approval file.
     *
     * @return The approval file path.
     */
    public Path getApprovalFile() {
        return approvalFile;
    }
}
