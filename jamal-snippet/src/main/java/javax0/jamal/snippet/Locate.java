package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.OptionsControlled;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Scanner;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Locate a file and return the file name (relative) where the file was found.
 */
public class Locate implements Macro, OptionsControlled, Scanner.WholeInput {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // the maximum number of directories to go up
        final var from = scanner.str(null, "in").defaultValue(null);
        final var filePattern = scanner.pattern("find", "match");
        final var typeSelector = scanner.bool(null, "isFile", "isDir", "isDirectory");
        final var full = scanner.bool(null, "fullPath");
        final var depth = scanner.number(null, "depth").defaultValue(Integer.MAX_VALUE);
        final var format = scanner.str("format").defaultValue("$name");
        final var dateFormat = scanner.str("dateFormat").defaultValue("yyyy-MM-dd HH:mm:ss");
        final var relativeTo = scanner.str("relativeTo").defaultValue(FilesMacro.getInputFileLocation(in));
        scanner.done();
        final var absPath = from.get() == null
                ?
                Paths.get(getLocalInputFileLocation(in))
                :
                Paths.get(FileTools.absolute(in.getReference(), from.get()));
        final var start = Files.isDirectory(absPath) ? absPath : absPath.getParent();
        final var fileOnly = typeSelector.isPresent() && typeSelector.is() && typeSelector.name().equals("isFile");
        final var dirOnly = typeSelector.isPresent() && typeSelector.is() && !typeSelector.name().equals("isFile");
        final var pattern = filePattern.get();
        final var fullPath = full.is();
        final var isMatch = filePattern.name().equals("match");
        try {
            final var foundFile = Files.walk(start, depth.get(), FileVisitOption.FOLLOW_LINKS)
                    .filter(f -> filter(f, fileOnly, dirOnly, fullPath, isMatch, pattern))
                    .map(Path::toFile).collect(Collectors.toList());
            BadSyntax.when(foundFile.isEmpty(), ("No file matching '" + pattern.pattern() + "' found in '" + start + "'"));
            BadSyntax.when(foundFile.size() > 1, ("There are multiple files matching '" + pattern.pattern() + "' found in '" + start + "'\n" +
                    foundFile.stream()
                            .map(File::getAbsolutePath)
                            .map(s -> "> " + s + "\n")
                            .collect(Collectors.joining(""))
            ));
            return FilesMacro.formatString(format.get(), foundFile.get(0).getName(), foundFile.get(0), dateFormat.get(), relativeTo.get());
        } catch (BadSyntax bs) {
            throw bs;
        } catch (Exception e) {
            throw new BadSyntax("Not possible to list the files in '" + start + "'", e);
        }
    }

    private String getLocalInputFileLocation(Input in) {
        if (in != null && in.getPosition() != null && in.getPosition().file != null) {
            return in.getPosition().file;
        }
        return new File(".").getAbsolutePath();
    }

    /**
     * Returns {@code true} when the path matches the criteria.
     * <p>
     * Return false if the path
     *
     * <ul>
     *     <li>{@code path} is not a file and {@code fileOnly} is {@code true}</li>
     *     <li>{@code path} is not a directory and {@code dirOnly} is {@code true}</li>
     *     <li>the {@code pattern} does not match </li>
     *     <ul>
     *         <li>the full path and {@code fullPath} is {@code true}</li>
     *         <li>the file name and {@code fullPath} is {@code false}</li>
     *     </ul>
     *     while using {@link Matcher#find()} when {@code isMatch} is {@code false} and {@link Matcher#matches()} when {@code isMatch} is {@code true}.
     * </ul>
     * <p>
     * In all other cases, it returns true.
     *
     * @param path     the path to check
     * @param fileOnly if {@code true} we need only files
     * @param dirOnly  if {@code true} we need only directories
     * @param fullPath if {@code true} we should use the pattern against the full path, otherwise only on the name
     * @param isMatch  if {@code true} we should use {@link Matcher#matches()} otherwise {@link Matcher#find()}
     * @param pattern  the pattern to match
     * @return {@code true} if the path matches the criteria
     */
    private static boolean filter(final Path path,
                                  final boolean fileOnly,
                                  final boolean dirOnly,
                                  final boolean fullPath,
                                  final boolean isMatch,
                                  final Pattern pattern) {
        if (fileOnly && !Files.isRegularFile(path)) {
            return false;
        }
        if (dirOnly && !Files.isDirectory(path)) {
            return false;
        }
        if (isMatch) {
            if (fullPath) {
                return pattern.matcher(path.toFile().getAbsolutePath()).matches();
            } else {
                return pattern.matcher(path.getFileName().toString()).matches();
            }
        } else {
            if (fullPath) {
                return pattern.matcher(path.toFile().getAbsolutePath()).find();
            } else {
                return pattern.matcher(path.getFileName().toString()).find();
            }
        }
    }

    @Override
    public String getId() {
        return "file:locate";
    }
}
