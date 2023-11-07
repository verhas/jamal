package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.api.Input;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.api.ResourceReader;
import javax0.jamal.api.ServiceLoaded;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax0.jamal.tools.Input.makeInput;

/**
 * Utility class containing static methods handling files.
 */
public class FileTools {


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
     * @param fileName  the name of the file. This is used to open and read the file as well as reference file name in
     *                  the input. When the file name starts with the characters {@code res:} then the rest of the string
     *                  is treated as the name of a Java resource. That way Jamal can load a Java resource from some JAR
     *                  that is on the classpath. If the file name starts with {@code https:} then the string is treated
     *                  as an URL. In that case the UTL is fetched and if there is a cache directory configured it will
     *                  be loaded from the cache.
     * @param processor is used to invoke the callback hooks registered for file access
     * @return the input containing the content of the file.
     * @throws BadSyntaxAt if the file cannot be read.
     */
    public static Input getInput(final String fileName, final Processor processor) throws BadSyntax {
        return getInput(fileName, false, processor);
    }

    public static Input getInput(final String fileName, final boolean noCache, Processor processor) throws BadSyntax {
        return makeInput(getFileContent(fileName, noCache, processor), new Position(fileName));
    }

    /**
     * Same as {@link #getInput(String, Processor)} but this method also specifies the parent position. It is usually the file
     * that includes or imports the other file that is being read.
     *
     * @param fileName  the name of the file to be read
     * @param parent    the parent/including/importing file position
     * @param processor is used to invoke the callback hooks registered for file access
     * @return the input containing the content of the file.
     * @throws BadSyntaxAt if the file cannot be read.
     */
    public static Input getInput(String fileName, Position parent, final Processor processor) throws BadSyntax {
        return getInput(fileName, parent, false, processor);
    }

    /**
     * This method is the same as {@link #getInput(String, Position, boolean, Processor)} but it
     * tries to load the file from different directories. The directories are specified in the {@code prefixes} array.
     * <p>
     * Technically, the method does not care if the strings represent a directory or not.
     * It simply tries each prefix with the file name concatenated and tries to load the file.
     * That way, the caller should care that the prefixes are directories, and if the file starts with '/', or the
     * prefixes end with one.
     *
     * @param prefixes  the prefixes to try to load the file from
     * @param fileName  the name of the file to be read. This file name is usually relative, and the absolute file name
     *                  will be calculated from the parent file name and this file name, for each prefix.
     * @param parent    the parent/including/importing file position. (See {@link #getInput(String, Position, boolean,
     *                  Processor)})
     * @param noCache   if {@code true} then the cache is not used to read the file.
     * @param processor is used to invoke the callback hooks registered for file access
     * @return the input containing the content of the file.
     * @throws BadSyntax when there is no file with noen of the prefix. In this case, all the BadSyntax exceptions
     *                   created during the different attempts are part of the final exception as suppressed exceptions.
     */
    public static Input getInput(final String[] prefixes,
                                 final String fileName,
                                 final Position parent,
                                 final boolean noCache,
                                 final Processor processor) throws BadSyntax {
        final var exceptions = new ArrayList<Throwable>();
        for (final var prefix : prefixes) {
            try {
                var absoluteFn = absolute(parent.file, prefix + fileName);
                return getInput(absoluteFn, parent, noCache, processor);
            } catch (BadSyntax e) {
                exceptions.add(e);
            }
        }
        throw new BadSyntax("Cannot read file '" + fileName + "' from any of the directories: "
                + String.join(", ", prefixes), exceptions);
    }

    /**
     * Get the input from the file.
     *
     * @param fileName  is the name of the file to get
     * @param parent    is the position of the input that needs the content of this file. It is used to calclate the
     *                  absolute file name in the case the file name is relative.
     * @param noCache   if {@code true} then the cache is not used to read the file.
     * @param processor is used to invoke the callback hooks registered for file access
     * @return the input object
     * @throws BadSyntax when the file cannot be read
     */
    public static Input getInput(String fileName, Position parent, final boolean noCache, final Processor processor) throws BadSyntax {
        return makeInput(getFileContent(fileName, noCache, processor), new Position(fileName, 1, 1, parent));
    }

    /**
     * Get the content of the file.
     *
     * @param fileName  the name of the file.
     * @param processor is used to invoke the callback hooks registered for file access
     * @return the content of the file
     * @throws BadSyntax if the file cannot be read
     */
    public static String getFileContent(String fileName, Processor processor) throws BadSyntax {
        return getFileContent(fileName, false, processor);
    }

    private static final List<ResourceReader> readers = ServiceLoaded.getInstances(ResourceReader.class);


    /**
     * Get the content of the file either reading it or from the cache or from the original source.
     * The cache is only consulted when the file is  a {@code http://} prefixed resource.
     *
     * @param fileName  the name of the file.
     * @param noCache   do not read the cache if this parameter is {@code true}. If there is cache configured the content
     *                  is still saved into the cache. It is only the reading controlled by the parameter.
     * @param processor is used to invoke the callback hooks registered for file access
     * @return the content of the file
     * @throws BadSyntax if the file cannot be read
     */
    public static String getFileContent(final String fileName, final boolean noCache, final Processor processor) throws BadSyntax {
        final var res = processor.getFileReader().map(reader -> reader.read(fileName)).orElse(Processor.IOHookResult.IGNORE);
        switch (res.type()) {
            case DONE:
                return res.get();
            case REDIRECT:
                final var content = getFileContent(res.get(), noCache, processor);
                processor.getFileReader().ifPresent(reader -> reader.set(fileName, content));
                return content;
            default:
                break;
        }
        if (readers.isEmpty()) {
            readers.addAll(ServiceLoaded.getInstances(ResourceReader.class));
        }
        try {
            readers.forEach(r -> r.setProcessor(processor));
            final String content =
                    readers.stream()
                            .filter(r -> r.canRead(fileName))
                            .findFirst()
                            .map(r -> {
                                try {
                                    return r.read(fileName, noCache);
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            }).orElseGet(() -> {
                                        try {
                                            return FileInput.getInput(fileName);
                                        } catch (IOException e) {
                                            throw new UncheckedIOException(e);
                                        }
                                    }
                            );
            processor.getFileReader().ifPresent(reader -> reader.set(fileName, content));
            return content;
        } catch (UncheckedIOException e) {
            throw new BadSyntax("Cannot get the content of the file '" + fileName + "'", e);
        }
    }

    public static byte[] getFileBinaryContent(final String fileName, final boolean noCache, final Processor processor) throws BadSyntax {
        final var res = processor.getFileReader().map(reader -> reader.read(fileName)).orElse(Processor.IOHookResult.IGNORE);
        switch (res.type()) {
            case DONE:
                return res.getBinary();
            case REDIRECT:
                final var content = getFileBinaryContent(res.get(), noCache, processor);
                processor.getFileReader().ifPresent(reader -> reader.set(fileName, content));
                return content;
            default:
                break;
        }
        if (readers.isEmpty()) {
            readers.addAll(ServiceLoaded.getInstances(ResourceReader.class));
        }
        try {
            readers.forEach(r -> r.setProcessor(processor));
            final byte[] content =
                    readers.stream()
                            .filter(r -> r.canRead(fileName))
                            .findFirst()
                            .map(r -> {
                                try {
                                    return r.readBinary(fileName, noCache);
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            }).orElseGet(() -> {
                                        try {
                                            return FileInput.getBinaryInput(fileName);
                                        } catch (IOException e) {
                                            throw new UncheckedIOException(e);
                                        }
                                    }
                            );
            processor.getFileReader().ifPresent(reader -> reader.set(fileName, content));
            return content;
        } catch (UncheckedIOException e) {
            throw new BadSyntax("Cannot get the content of the file '" + fileName + "'", e);
        }
    }

    public static void writeFileContent(String fileName, String content, final Processor processor) throws BadSyntax {
        final String finalFileName;
        final var res = processor.getFileWriter().map(s -> s.write(fileName, content)).orElse(Processor.IOHookResult.IGNORE);
        switch (res.type()) {
            case DONE:
                return;
            case REDIRECT:
                writeFileContent(res.get(), content, processor);
                return;
            default:
                finalFileName = fileName;
                break;
        }
        try {
            if (readers.stream().anyMatch(r -> r.canRead(fileName))) {
                throw new BadSyntax("Cannot write into a resource.");
            }
            BadSyntax.when(finalFileName.startsWith(HTTPS_PREFIX), "Cannot write into a web resource.");
            File file = new File(finalFileName);
            if (file.getParentFile() != null) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }
            try (final var fos = new FileOutputStream(file)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new BadSyntax("Cannot get the content of the file '" + finalFileName + "'", e);
        }
    }

    private static final Map<String, String> devPaths = new HashMap<>();

    static {
        EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_DEV_PATH_ENV).ifPresent(devPath -> {
            final String[] paths;
            if (new File(devPath).exists()) {
                try {
                    paths = Files.readString(Paths.get(devPath), StandardCharsets.UTF_8).split("\n", -1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                paths = InputHandler.getParts(makeInput(devPath));
            }
            for (String path : paths) {
                // skip empty and comment lines in case we read from file
                if (path.trim().length() == 0 || path.trim().startsWith("#")) {
                    continue;
                }
                final var parts = path.split("=", 2);
                if (parts.length == 2) {
                    devPaths.put(parts[0], parts[1]);
                } else {
                    throw new RuntimeException("Invalid dev path: " + path);
                }
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
            int i = fileStart(unixedReference);
            if (i >= 0) {
                prefix = unixedReference.substring(0, i);
                unprefixedReference = unixedReference.substring(i);
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

    /**
     * Convert the file name to an absolute file name if it starts with a {@code ~} character. The {@code ~} character
     * denotes the home directory of the user. The home directory is determined by the system property {@code
     * user.home}. It is used usually by the shell, but the file handling system calls don't honour this notation.
     *
     * @param fileName optionally containing a {@code ~} character at the start
     * @return the original file name, or the file name replacing the {@code ~} character with the home directory of the
     * user
     */
    public static String adjustedFileName(final String fileName) {
        if (fileName.charAt(0) == '~' && fileName.charAt(1) == '/') {
            return System.getProperty("user.home") + fileName.substring(1);
        }
        return fileName;
    }

    /**
     * Calculates the relative path of a file with respect to another file.
     * <p>
     * This version of the algorithm also calculates the target file is not under the directory of the base file.
     * The {@link URI#relativize(URI)} method returns the absolute path and does not insert ".." parts into the returned path.
     * <p>
     * If the base and the target files are in totally different directories, meaning that there is no common part at the
     * start of the path, then the result will contain so many ".." elements as many is needed to get to the root.
     * This is not a realistic case, but it still may happen with some containerized environments.
     * However, the generated file, where the relative path is used may not be containerized and the absolute path would
     * not work there.
     *
     * @param baseFile   The base file to which the relative path is calculated.
     * @param targetFile The target file whose relative path is to be found.
     * @return The relative path of the target file with respect to the base file.
     */
    public static String getRelativePath(File baseFile, File targetFile) {
        final var base = baseFile.getAbsolutePath().split("[/\\\\]");
        final var baseLength = baseFile.isFile() ? base.length - 1 : base.length;
        final var target = targetFile.getAbsolutePath().split("[/\\\\]");
        for (int i = 0; i < baseLength && i < target.length; i++) {
            if (!base[i].equals(target[i])) {
                final var sb = new StringBuilder();
                sb.append("../".repeat(baseLength - 1 - i));
                for (int j = i; j < target.length; j++) {
                    sb.append(target[j]);
                    if (j < target.length - 1) {
                        sb.append("/");
                    }
                }
                return sb.toString();
            }
        }
        return target[target.length - 1];
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
        return prefixEnd(fileName) >= 0;
    }

    /**
     * Check if the name of the file has to be interpreted as a remote file (or Java resource).
     * <p>
     * The actualimplementation checks that the resource type is at the start of the file name with a ':' character
     * following it. The resource type is a sequence of alphabetic characters. It has to be at least two characters.
     *
     * @param fileName the file name to check.
     * @return index of the ':' character following the resource type, e.g. 'res:', 'https:', or -1 if the file name is
     * local, simple file
     */
    private static int prefixEnd(final String fileName) {
        int i = fileName.indexOf(':');
        if (i < 2) { // -1 none, 0 starts with, 1 Windows drive letter
            return -1;
        }
        for (int j = 0; j < i; j++) {
            if (!Character.isAlphabetic(fileName.charAt(j))) {
                return -1;
            }
        }
        return i;
    }

    /**
     * Return the character index where the file name used to calculate the absolute file name from the reference and the
     * relative file name has to start.
     *
     * @param fileName the full file name including the prefix and all the parts before the file name
     * @return the index, which is the first character of the file name
     */
    private static int fileStart(final String fileName) {
        if (fileName.startsWith("https://")) {
            return 7; // number of characters
        }
        return readers.stream().filter(r -> r.canRead(fileName)).findFirst().map(r -> r.fileStart(fileName)).orElse(-1);
    }

    /**
     * Add a trailing {@code /} at the end of the directory name if it is not there yet.
     *
     * @param dir the directory name
     * @return the name of the directory guaranteed having a tailing {@code /} at the end
     */
    public static String trailDirectory(final String dir) {
        return dir.isEmpty() || dir.endsWith("/") ? dir : dir + "/";
    }

}
