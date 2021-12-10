package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.api.Input;
import javax0.jamal.api.Position;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static javax0.jamal.tools.Input.makeInput;

/**
 * Utility class containing static methods handling files.
 */
public class FileTools {

    private static final String RESOURCE_PREFIX = "res:";
    private static final int RESOURCE_PREFIX_LENGTH = RESOURCE_PREFIX.length();
    private static final String HTTPS_PREFIX = "https://";

    /**
     * Create a new input from a file.
     * <p>
     * Reads the file and returns an input object that has the content of the file.
     * <p>
     * The file can be
     * <ul>
     *     <li>a plain file,
     *     </li>
     *     <li>a Java resource (file name starts with {@code res:}, or
     *     </li>
     *     <li>a {@code https} downloadable content (file name starts with {@code https}.
     *     </li>
     * </ul>
     * <p>
     * If the content comes from a https URL then the local cache is checked before.
     * <p>
     * There is no cache eviction. All files donwloaded once are in the cache and the remote is never checked again.
     * If the URL contains the string literal {@code SNAPSHOT} (all capital letters) it is not cached.
     * <p>
     * There is no way to download a resource using the {@code http} protocol.
     *
     * @param fileName the name of the file. This is used to open and read the file as well as reference file name in
     *                 the input. When the file name starts with the characters {@code res:} then the rest of the string
     *                 is treated as the name of a Java resource. That way Jamal can load a Java resource from some JAR
     *                 that is on the classpath. If the file name starts with {@code https:} then the string is treated
     *                 as an URL. In that case the UTL is fetched and if there is a cache directory configured it will
     *                 be loaded from the cache.
     * @return the input containing the content of the file.
     * @throws BadSyntaxAt if the file cannot be read.
     */
    public static Input getInput(String fileName) throws BadSyntax {
        return makeInput(getFileContent(fileName), new Position(fileName));
    }

    /**
     * Same as {@link #getInput(String)} but this metjod also specifies the parent position. It is usually the file
     * that includes or imports the file that is being read.
     *
     * @param fileName the name of the file to be read
     * @param parent the parent/including/importing file position
     * @return the input containing the content of the file.
     * @throws BadSyntaxAt if the file cannot be read.
     */
    public static Input getInput(String fileName, Position parent) throws BadSyntax {
        return makeInput(getFileContent(fileName), new Position(fileName, 1, 1, parent));
    }

    /**
     * Get the content of the file.
     *
     * @param fileName the name of the file.
     * @return the content of the file
     * @throws BadSyntax if the file cannot be read
     */
    public static String getFileContent(String fileName) throws BadSyntax {
        try {
            if (fileName.startsWith(RESOURCE_PREFIX)) {
                return ResourceInput.getInput(fileName.substring(RESOURCE_PREFIX_LENGTH));
            }
            if (fileName.startsWith(HTTPS_PREFIX)) {
                return CachedHttpInput.getInput(fileName).toString();
            } else {
                return FileInput.getInput(fileName);
            }
        } catch (IOException| UncheckedIOException e) {
            throw new BadSyntax("Cannot get the content of the file '" + fileName + "'", e);
        }
    }

    public static void writeFileContent(String fileName, String content) throws BadSyntax {
        try {
            if (fileName.startsWith(RESOURCE_PREFIX)) {
                throw new BadSyntax("Cannot write into a resource.");
            }
            if (fileName.startsWith(HTTPS_PREFIX)) {
                throw new BadSyntax("Cannot write into a web resource.");
            } else {
                File file = new File(fileName);
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                try (final var fos = new FileOutputStream(file)) {
                    fos.write(content.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            throw new BadSyntax("Cannot get the content of the file '" + fileName + "'", e);
        }
    }

    private static final Map<String, String> devPaths = new HashMap<>();

    static {
        EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_DEV_PATH_ENV).ifPresent(devPath -> {
            try {
                final String[] paths;
                paths = InputHandler.getParts(makeInput(devPath));
                for (String path : paths) {
                    final var parts = path.split("=", 2);
                    if (parts.length == 2) {
                        devPaths.put(parts[0], parts[1]);
                    } else {
                        throw new RuntimeException("Invalid dev path: " + path);
                    }
                }
            } catch (BadSyntaxAt e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Convert the file name to an absolute file name if it is relative to the directory containing the reference file.
     * Note that {@code reference} is the name of a file and not a directory.
     * <p>
     * If the name of the file starts with one of the characters:
     * <ul>
     * <li>{@code /}</li>
     * <li>{@code \}</li>
     * <li>{@code ~}</li>
     * </ul>
     * <p>
     * or starts with an alpha character and a {@code :} (DOS drive letter, like {@code C:},
     * <p>
     * or starts with the resource prefix {@code res:},
     * <p>
     * or starts with the HTTPS prefix {@code https:},
     * <p>
     * then the file name is absolute.
     * <p>
     * Absolute file names are not modified usually. If the environment variable {@code JAMAL_DEV_PATH} is set, or the
     * system property {@code jamal.dev.path} is set, then the file name is replaced by the value specified as a
     * replacement in the variable if any.
     * <p>
     * Otherwise the string in the parameter {@code reference} is used as it was a file name (the file does not need to
     * exist) and {@code file} is treated as a relative file name and the absolute path is calculated.
     *
     * @param reference the name of the reference file
     * @param fileName  the name of the file, absolute or relative
     * @return the absolute file name of the file
     */
    public static String absolute(final String reference, final String fileName) {
        if (isAbsolute(fileName)) {
            return adjustedFileName(devPaths.getOrDefault(fileName, fileName));
        } else {
            final var unixedReference = reference == null ? "." : reference.replaceAll("\\\\", "/");
            final String prefix;
            final String unprefixedReference;
            if (unixedReference.startsWith(HTTPS_PREFIX)) {
                unprefixedReference = unixedReference.substring((HTTPS_PREFIX).length());
                prefix = HTTPS_PREFIX;
            } else if (unixedReference.startsWith(RESOURCE_PREFIX)) {
                unprefixedReference = unixedReference.substring((RESOURCE_PREFIX).length());
                prefix = RESOURCE_PREFIX;
            } else {
                prefix = "";
                unprefixedReference = unixedReference;
            }
            final var referencePath = unprefixedReference.contains("/") ?
                unprefixedReference.substring(0, unprefixedReference.lastIndexOf("/") + 1)
                : "";
            return prefix + Paths.get(referencePath)
                .resolve(Paths.get(fileName))
                .normalize()
                .toString()
                .replaceAll("\\\\", "/");
        }
    }

    public static String adjustedFileName(final String fileName) {
        if (fileName.charAt(0) == '~' && fileName.charAt(1) == '/') {
            return System.getProperty("user.home") + fileName.substring(1);
        }
        return fileName;
    }


    /**
     * Check if the name of the file has to be interpreted as an absolute filename or not. This is not the same as any
     * JDK provided method, because it checks the {@code res://} and {@code https://} prefix as well and also the {@code
     * ~/} at the start, which is usually resolved by the shell, but Jamal file handling resolves it so that Jamal files
     * can also use the {@code ~/... } file format.
     *
     * @param fileName the file name to check.
     * @return {@code true} if the file name should be treated as an absolute file name and {@code false} otherwise
     */
    public static boolean isAbsolute(String fileName) {
        return isRemote(fileName) ||
            fileName.startsWith("/") ||
            fileName.startsWith("\\") ||
            fileName.startsWith("~") ||
            (fileName.length() > 1 &&
                Character.isAlphabetic(fileName.charAt(0))
                && fileName.charAt(1) == ':');
    }

    /**
     * Check if the name of the file has to be interpreted as a remote file or as a resource.
     *
     * @param fileName the file name to check.
     * @return {@code true} if the file name should be treated as a remote file and {@code false} otherwise
     */
    public static boolean isRemote(String fileName) {
        return fileName.startsWith(HTTPS_PREFIX) ||
            fileName.startsWith(RESOURCE_PREFIX);
    }

    /**
     * Add a trailing {@code /} at the end of the directory name if it is not there yet.
     *
     * @param dir the directory name
     * @return the name of the directory guaranteed having a tailing {@code /} at the end
     */
    public static String trailDirectory(final String dir) {
        return dir.length() == 0 || dir.endsWith("/") ? dir : dir + "/";
    }

}
