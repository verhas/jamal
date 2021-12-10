package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.PlaceHolders;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.Params.holder;

public class ListDir implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var format = holder("format").orElse("$name").asString();
        final var separator = holder("separator", "sep").orElse(",").asString();
        final var grep = holder("grep").orElse(null).asString();
        final var glob = holder("pattern").orElse(null).asString();
        final var maxDepth = holder("maxDepth").orElseInt(Integer.MAX_VALUE);
        final var isFollowSymlinks = holder("followSymlinks").asBoolean();
        final var countOnly = holder("countOnly").asBoolean();
        Params.using(processor).from(this).between("()")
            .keys(format, maxDepth, isFollowSymlinks, separator, grep, glob, countOnly).parse(in);

        final FileVisitOption[] options;
        if (isFollowSymlinks.get()) {
            options = new FileVisitOption[]{FileVisitOption.FOLLOW_LINKS};
        } else {
            options = new FileVisitOption[0];
        }

        final var grepPattern = grep.get() == null ? null : Pattern.compile(grep.get());
        final var globPattern = glob.get() == null ? null : Pattern.compile(glob.get());

        skipWhiteSpaces(in);
        final var reference = in.getReference();
        var dirName = FileTools.absolute(reference, in.toString().trim());

        var dir = new File(dirName);
        if (!dir.isDirectory()) {
            throw new BadSyntaxAt("'" + dirName + "' does not seem to be a directory to list", in.getPosition());
        }

        try {
            final var fmt = format.get();
            final var stream = Files.walk(Paths.get(dirName), maxDepth.get(), options)
                .filter(p -> grep(p, grepPattern))
                .filter(p -> glob(p, globPattern))
                .map(p -> format(p, fmt));
            if (countOnly.is()) {
                return "" + stream.count();
            } else {
                return stream.collect(Collectors.joining(separator.get()));
            }
        } catch (Exception e) {
            throw new BadSyntaxAt("There was an IOException listing the files '" + in + "'", in.getPosition(), e);
        }
    }

    private static boolean glob(Path p, Pattern pattern) {
        return pattern == null || pattern.matcher(p.toFile().getAbsolutePath()).find();
    }

    private static boolean grep(Path p, Pattern pattern) {
        try {
            if (pattern == null || p.toFile().isDirectory()) {
                return true;
            }
            return pattern.matcher(Files.readString(p, StandardCharsets.UTF_8)).find();
        } catch (IOException| UncheckedIOException e) {
            return false;
        }
    }

    private static String format(Path p, String format) {
        String size;
        try {
            size = "" + Files.size(p);
        } catch (IOException|UncheckedIOException e) {
            size = "0";
        }
        String time;
        try {
            time = "" + Files.getLastModifiedTime(p);
        } catch (IOException|UncheckedIOException e) {
            // snippet defaultTimeForListDir
            time = "1970-01-01T00:00:00Z";
            //end snippet
        }
        try {
            return PlaceHolders.with(
                // OTF will be replaced by "of the file"
                // TITF will be replaced by "`true` if the file"
                // FO will be replaced by "``false` otherwise"
                // snippet listDirFormats
                "$size", size, // size OTF
                "$time", time, // modification time OTF
                "$absolutePath", p.toAbsolutePath().toString(), // absolute path OTF
                "$name", p.toString(), // name OTF
                "$simpleName", p.toFile().getName(), // simple name OTF
                "$isDirectory", "" + p.toFile().isDirectory(), // TITF is a directory, FO
                "$isFile", "" + p.toFile().isFile(), // TITF is a plain file, FO
                "$isHidden", "" + p.toFile().isHidden(), // TITF is hidden, FO
                "$canExecute", "" + p.toFile().canExecute(), // TITF can be executed, FO
                "$canRead", "" + p.toFile().canRead(), // TIFT can be read, FO
                "$canWrite", "" + p.toFile().canWrite() //TITF can be written, FO
                // end snippet
            ).format(format);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getId() {
        return "listDir";
    }
}
