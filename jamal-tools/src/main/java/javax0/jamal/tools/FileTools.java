package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Position;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.BindException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static javax0.jamal.tools.Input.makeInput;

/**
 * Utility class containing static methods handling files.
 */
public class FileTools {

    private static final String RESOURCE_PREFIX = "res:";
    private static final int RESOURCE_PREFIX_LENGTH = RESOURCE_PREFIX.length();
    private static final String HTTPS_PREFIX = "https:";

    /**
     * Create a new input from a file.
     *
     * @param fileName the name of the file. This is used to open and read the file as well as reference file name in
     *                 the input. When the file name starts with the characters {@code res:} then the rest of the string
     *                 is treated as the name of a Java resource. That way Jamal can load a Java resource from some JAR
     *                 that is on the classpath.
     * @return the input containing the contend of the file.
     * @throws BadSyntaxAt if the file cannot be read.
     */
    public static Input getInput(String fileName) throws BadSyntax {
        try {
            if (fileName.startsWith(RESOURCE_PREFIX)) {
                return makeInput(getResourceInput(fileName.substring(RESOURCE_PREFIX_LENGTH)), new Position(fileName));
            }
            if (fileName.startsWith(HTTPS_PREFIX)) {
                return makeInput(CachedHttpInput.geInput(fileName), new Position(fileName));
            } else {
                return makeInput(Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n")),
                    new Position(fileName));
            }
        } catch (IOException e) {
            throw new BadSyntax("Cannot get the content of the file '" + fileName + "'",e);
        }
    }

    /**
     * Read the content of the resource as a UTF-8 encoded character stream
     *
     * @param fileName the name of the resource (already without the {@code res:} prefix
     * @return the content of the resource as a string
     * @throws IOException if the resource cannot be read
     */
    private static String getResourceInput(String fileName) throws IOException {
        try (final var is = FileTools.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new BindException("The resource file 'res:" + fileName + "' cannot be read.");
            }
            final var reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            final var writer = new StringWriter();
            reader.transferTo(writer);
            return writer.toString();
        }
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
     * or starts with an alpha character and a {@code :} (DOS drive letter, like {@code C:} then the file name is
     * absolute and it is returned as it is.
     * <p>
     * Otherwise the string in the parameter {@code reference} is used as it was a file name (the file does not need to
     * exist) and {@code file} is treated as a relative file name and the absolute path is calculated.
     *
     * @param reference the name of the reference file
     * @param fileName  the name of the file, absolute or relative
     * @return the absolute file name of the file
     */
    public static String absolute(final String reference, String fileName) {
        if (fileName.startsWith(RESOURCE_PREFIX) ||
            fileName.startsWith(HTTPS_PREFIX) ||
            fileName.startsWith("/") ||
            fileName.startsWith("\\") ||
            fileName.startsWith("~") ||
            (fileName.length() > 1 &&
                Character.isAlphabetic(fileName.charAt(0))
                && fileName.charAt(1) == ':')) {
            return fileName;
        }
        final var unixedReference = reference.replaceAll("\\\\", "/");
        final var referencePath = unixedReference.contains("/") ?
            unixedReference.substring(0, unixedReference.lastIndexOf("/") + 1)
            : "";
        return Paths.get(referencePath)
            .resolve(Paths.get(fileName))
            .normalize()
            .toString()
            .replaceAll("\\\\", "/");
    }
}
