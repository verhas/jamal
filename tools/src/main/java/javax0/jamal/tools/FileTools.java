package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Utility class containing static methods handling files.
 */
public class FileTools {

    /**
     * Create a new input from a file.
     * @param fileName the name of the file. This is used to open and read the file as well as reference file name in the input.
     * @return the input containing the contend of the file.
     * @throws BadSyntax if the file cannot be read.
     */
    public static Input getInput(String fileName) throws BadSyntax {
        try {
            return new javax0.jamal.tools.Input(new StringBuilder(Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"))), fileName);
        } catch (IOException e) {
            throw new BadSyntax("Cannot get the content of the file '" + fileName + "'");
        }
    }

    /**
     * Convert the file name to an absolute file name if it is relative to the directory containing the reference file.
     * Note that {@code reference} is the name of a file and not a directory.
     * <p>
     * If the name of the file starts with one of the characters:
     * <ul>
     *     <li>{@code /}</li>
     *     <li>{@code \}</li>
     *     <li>{@code ~}</li>
     * </ul>
     *
     * or starts with an alpha character and a {@code :} (DOS drive letter, like {@code C:} then the file name is
     * absolute and it is returned as it is.
     * <p>
     * Otherwise the string in the parameter {@code reference} is used as it was a file name (the file does not need to
     * exist) and {@code fileName} is treated as a relative file name and the absolute path is calculated.
     *
     * @param reference the name of the reference file
     * @param fileName the name of the file, absolute or relative
     * @return the absolute file name of the file
     */
    public static String absolute(String reference, String fileName) {
        if (fileName.startsWith("/") ||
            fileName.startsWith("\\") ||
            fileName.startsWith("~") ||
            (fileName.length() > 1 &&
                Character.isAlphabetic(fileName.charAt(0))
                && fileName.charAt(1) == ':')) {
            return fileName;
        }
        var z = reference.lastIndexOf("/");
        if (z > -1) {
            reference = reference.substring(0, z + 1);
        } else {
            reference = "";
        }

        return Paths.get(reference)
                .resolve(Paths.get(fileName))
                .normalize()
                .toString()
                .replaceAll("\\\\","/");
    }
}
