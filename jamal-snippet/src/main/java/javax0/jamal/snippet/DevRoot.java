package javax0.jamal.snippet;

import javax0.jamal.DocumentConverter;
import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.ListParameter;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Macro.Name("dev:root")
public class DevRoot implements Macro, OptionsControlled, Scanner.WholeInput {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var placeholders = scanner.list(null, "placeholder", "placeholders");
        final var format = scanner.str("format").defaultValue("$absolutePath");
        final var dateFormat = scanner.str("dateFormat").defaultValue("yyyy-MM-dd HH:mm:ss");
        final var relativeTo = scanner.str("relativeTo").defaultValue(FilesMacro.getInputFileLocation(in));
        scanner.done();
        final var fileNames = collectFileNames(placeholders);
        for (var cwd = new File(relativeTo.get()); cwd != null; cwd = cwd.getParentFile()) {
            for (final var file : fileNames) {
                if (new File(cwd, file).exists()) {
                    try {
                        return FilesMacro.formatString(format.get(), cwd.getName(), cwd, dateFormat.get(), relativeTo.get());
                    } catch (Exception e) {
                        throw new BadSyntax("Can not format the root directory using the format '" + format.get() + "'", e);
                    }

                }
            }
        }
        throw new BadSyntax("Cannot find project root.");
    }

    private Set<String> collectFileNames(ListParameter placeholders) throws BadSyntax {
        if (placeholders.isPresent() && !placeholders.get().isEmpty()) {
            final var fileNames = new HashSet<String>();
            for (final var s : placeholders.get()) {
                fileNames.addAll(
                        Set.of(
                                Arrays.stream(s.split(","))
                                        .map(String::trim)
                                        .toArray(String[]::new)));
            }
            return fileNames;
        } else {
            return Set.of(InputHandler.PLACEHOLDERS);
        }
    }
}
