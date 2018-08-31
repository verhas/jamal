package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FileTools {

    public static Input getInput(String fileName) throws BadSyntax {
        try {
            return new javax0.jamal.tools.Input(new StringBuilder(Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"))), fileName);
        } catch (IOException e) {
            throw new BadSyntax("Cannot get the content of the file '" + fileName + "'");
        }
    }

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
