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
        if (fileName.startsWith("./")) {
            fileName = fileName.substring(2);
        }
        var z = reference.lastIndexOf("/");
        if (z > -1) {
            reference = reference.substring(0, z + 1);
        } else {
            reference = "";
        }

        var absoluteFileName = (reference + fileName).replaceAll("//", "/");
        var path = new ArrayList<>(Arrays.asList(absoluteFileName.split("/")));
        var i = 0;
        while (i < path.size() - 1) {
            if (".".equals(path.get(i))) {
                path.remove(i);
            } else if (!"..".equals(path.get(i)) && "..".equals(path.get(i + 1))) {
                path.remove(i + 1);
                path.remove(i);
                if (i > 0) {
                    i--;
                }
            } else {
                i++;
            }
        }
        return String.join("/", path);
    }

}
