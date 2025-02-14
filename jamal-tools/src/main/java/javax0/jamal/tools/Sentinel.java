package javax0.jamal.tools;

import javax0.jamal.api.Input;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.util.Set;

public class Sentinel {
    private Path approvalFile;

    private final Input input;
    private String type = null;

    public String getErrorMessage() {
        return message;
    }

    private String message = null;

    public static Sentinel forThe(final Input input) {
        return new Sentinel(input);
    }

    public Sentinel withType(final String type) {
        this.type = type;
        result = _check();
        return this;
    }

    private Sentinel(Input input) {
        this.input = input;
    }

    private boolean result;
    public boolean check() {
        return result;
    }

    private boolean _check() {
        try {
            final var root = InputHandler.getRootDir(input);
            if (root == null) {
                this.approvalFile = getValidSentinel(type, InputHandler.getInputFileLocation(input));
            } else {
                this.approvalFile = getValidSentinel(type, InputHandler.getInputFileLocation(input), root);
            }
            return approvalFile != null;
        } catch (IOException e) {
            return false;
        }
    }

    public Path getValidSentinel(final String type, final File... directories) {
        final Path[] approvalFiles = new Path[directories.length];
        int i = 0;
        for (var projectRoot : directories) {
            if (projectRoot == null || !projectRoot.isDirectory()) {
                continue;
            }

            final var approvalFile = projectRoot.toPath().resolve(sentinelName(type));
            approvalFiles[i++] = approvalFile;
            if (!Files.exists(approvalFile)) {
                continue;
            }

            if (!hasValidPermissions(approvalFile)) {
                this.message = errorMessage("Approval file permissions are incorrect", approvalFile);
                return null;
            }
            return approvalFile;
        }
        this.message = errorMessage("Approval file does not exist", approvalFiles);
        return null;
    }

    private static String sentinelName(final String type) {
        return "." + type + ".sentinel";
    }

    /**
     * Checks if the approval file has valid permissions.
     *
     * <p>Valid permissions are:
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
     * @param files  The file paths.
     * @return The formatted error message.
     */
    private String errorMessage(String reason, Path... files) {
        final var sb = new StringBuilder();
        sb.append(String.format("%s: Approval file required at:\n", reason));
        String s = "";
        for (Path file : files) {
            final var f = getRealPath(file).getAbsolutePath();
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                sb.append(String.format("%s   echo. > %s & attrib +R %s\n", s, f, f));
            } else {
                sb.append(String.format("%s   touch %s; chmod 400 %s\n", s, f, f));
            }
            s = "            or\n";
        }
        return sb.toString();
    }

    /**
     * Retrieves the real (canonical) path of a given file.
     *
     * @param file The file path.
     * @return The canonical file object, or the original file if resolution fails.
     */
    private File getRealPath(Path file) {
        try {
            return file.toFile().getCanonicalFile();
        } catch (IOException e) {
            return file.toFile();
        }
    }


}

