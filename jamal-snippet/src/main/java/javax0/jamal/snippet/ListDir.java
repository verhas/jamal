package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.MacroReader;
import javax0.jamal.tools.OptionsStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class ListDir implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var reader = MacroReader.macro(processor);
        final var iReader = reader.integer();

        final var format = reader.readValue("format").orElse("$name");
        final var optionsStore = OptionsStore.getInstance(processor);
        final var isFollowSymlinks = optionsStore.is("followSymlinks");
        final var maxDepth = iReader.readValue("maxDepth").orElse(Integer.MAX_VALUE);
        final FileVisitOption[] options;
        if (isFollowSymlinks) {
            options = new FileVisitOption[1];
            options[0] = FileVisitOption.FOLLOW_LINKS;
        } else {
            options = new FileVisitOption[0];
        }

        skipWhiteSpaces(in);
        final var reference = in.getReference();
        var dirName = FileTools.absolute(reference, in.toString().trim());

        var dir = new File(dirName);
        if (!dir.isDirectory()) {
            throw new BadSyntaxAt("'" + dirName + "' does not seem to be a directory to list", in.getPosition());
        }

        try {
            return Files.walk(Paths.get(dirName), maxDepth, options)
                .map(p -> format(p, format))
                .collect(Collectors.joining(","));
        } catch (IOException e) {
            throw new BadSyntaxAt("There was an IOException listing the files '" + in.toString() + "'", in.getPosition(), e);
        }
    }

    private static String format(Path p, String format) {
        var s = format
            .replace("$absolutePath", p.toAbsolutePath().toString())
            .replace("$name", p.toString())
            .replace("$simpleName", p.toFile().getName())
            .replace("$isDirectory", "" + p.toFile().isDirectory())
            .replace("$isFile", "" + p.toFile().isFile())
            .replace("$isHidden", "" + p.toFile().isHidden())
            .replace("$canExecute", "" + p.toFile().canExecute())
            .replace("$canRead", "" + p.toFile().canRead())
            .replace("$canWrite", "" + p.toFile().canWrite());
        try {
            s = s.replace("$size", "" + Files.size(p));
        } catch (IOException e) {
            s = s.replace("$size", "0");
        }
        try {
            s = s.replace("$time", "" + Files.getLastModifiedTime(p));
        } catch (IOException e) {
            s = s.replace("$time", "1970-01-01T00:00:00Z");
        }
        return s;

    }

    @Override
    public String getId() {
        return "listDir";
    }
}
