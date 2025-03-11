package javax0.jamal.tools;

import javax0.jamal.api.*;
import javax0.jamal.api.Input;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.util.Objects;
import java.util.Set;

public class Sentinel {

    /**
     * A macro that checks the existence of a sentinel file, and if it is okay, then it invokes the underlying macro.
     */
    public static class CheckerProxy implements Macro {

        private final Macro macro;
        private final String type;

        public CheckerProxy(Macro macro, String type) {
            this.macro = macro;
            this.type = type;
        }

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var sentinel = javax0.jamal.tools.Sentinel.forThe(in, processor).withType(type);
            BadSyntax.when(!sentinel.check(), sentinel.getErrorMessage());
            return macro.evaluate(in, processor);
        }
    }

    public static class InnerScopeDependentCheckerProxy extends CheckerProxy implements InnerScopeDependent {
        public InnerScopeDependentCheckerProxy(Macro macro, String type) {
            super(macro, type);
        }
    }


    public static Macro proxy(Macro macro, String type) {
        if (macro instanceof InnerScopeDependent) {
            return new InnerScopeDependentCheckerProxy(macro, type);
        } else {
            return new CheckerProxy(macro, type);
        }
    }

    private final Input input;
    private final Processor processor;

    public String getErrorMessage() {
        return message;
    }

    private String message = null;

    public static Sentinel forThe(final Input input, final Processor processor) {
        return new Sentinel(input, processor);
    }

    /**
     * Sentinel context keeps track of the sentinel check and avoids checking the existence of the sentinel file
     * and the permission set if it was already checked.
     * <p>
     * This context is a local context indexed by a {@link SentinelContext.Key} containing the type of the sentinel and
     * the input. It is stored in the processor local context map.
     */
    private static class SentinelContext implements Context {

        final boolean result;

        private SentinelContext(boolean result) {
            this.result = result;
        }

        private static class Key {

            private final String type;
            private final Input input;

            private Key(String type, Input input) {
                this.type = type;
                this.input = input;

            }

            @Override
            public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass()) return false;
                Key that = (Key) o;
                return Objects.equals(type, that.type) && Objects.equals(input, that.input);
            }

            @Override
            public int hashCode() {
                return Objects.hash(type, input);
            }

        }

        public static Key key(String type, Input input) {
            return new Key(type, input);
        }
    }

    private boolean result;

    /**
     * Create the sentinel object and also check that the sentinel file exists and has the proper permission.
     * <p>
     * If the local contexts contain a sentinel object already for the same input and type, then the file check is
     * skipped and the already established boolean value will be in the sentinel object.
     *
     * @param type the type of the sentinel. This is the `.<type>.sentinel` part of the name of the file.
     * @return the sentinel object
     */
    public Sentinel withType(final String type) {
        result = processor.getLocalContext(SentinelContext.key(type, input), () -> new SentinelContext(_check(type))).result;
        return this;
    }

    private Sentinel(Input input, Processor processor) {
        this.input = input;
        this.processor = processor;
    }


    public boolean check() {
        return result;
    }

    private boolean _check(final String type) {
        try {
            final var root = InputHandler.getRootDir(input);
            final Path approvalFile;
            if (root == null) {
                approvalFile = getValidSentinel(type, InputHandler.getInputFileLocation(input));
            } else {
                approvalFile = getValidSentinel(type, InputHandler.getInputFileLocation(input), root);
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

