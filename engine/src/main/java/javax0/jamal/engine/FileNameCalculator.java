package javax0.jamal.engine;

import java.util.ArrayList;
import java.util.Arrays;

public class FileNameCalculator {

    public static String absolute(String reference, String fileName) {
        if (fileName.startsWith("/") ||
            fileName.startsWith("\\") ||
            fileName.startsWith("~/") ||
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
        var path = new ArrayList<String>(Arrays.asList(absoluteFileName.split("/")));
        var normalized = new ArrayList<String>();
        while (path.size() > 0) {
            if (path.size() == 1) {
                normalized.add(path.get(0));
                break;
            }
            if (!"..".equals(path.get(0)) && "..".equals(path.get(1))) {
                path.remove(0);
                path.remove(0);
            } else {
                normalized.add(path.remove(0));
            }
        }
        return String.join("/",normalized);
    }

}
