package javax0.jamal.io;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class Remove implements Macro, InnerScopeDependent, Scanner.FirstLine {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var file = Utils.getFile(scanner);
        final var recursive = Utils.getRecursive(scanner);
        scanner.done();

        final var fileName = Utils.getFile(file, in);

        try {
            final var allDeleted = new AtomicBoolean(true);
            final var errors = new StringBuilder();
            final var fileList = new StringBuilder();
            if (recursive.is()) {
                Files.walk(Paths.get(fileName))
                        .map(Path::toFile)
                        .sorted((o1, o2) -> -o1.compareTo(o2))
                        .forEach(f -> allDeleted.set(allDeleted.get() && remove(f, errors, fileList)));
                BadSyntax.when(!allDeleted.get(), () -> String.format("Not possible to delete the file/dir and all files/dirs under '%s'\n%s\n%s\n",
                        fileName, fileList, errors));
            } else {
                Files.delete(Paths.get(fileName));
            }
        } catch (IOException ioException) {
            throw new BadSyntax("Not possible to delete '" + fileName + "'", ioException);
        }
        return "";
    }

    private static boolean remove(final File f, final StringBuilder errors, final StringBuilder fileList) {
        fileList.append(f.getAbsoluteFile()).append("\n");
        try {
            Files.delete(f.toPath());
            return true;
        } catch (java.nio.file.DirectoryNotEmptyException dne) {
            try (final var sw = new StringWriter();
                 final var pw = new PrintWriter(sw)) {
                dne.printStackTrace(pw);
                errors.append(sw);
                errors.append(String.format("Files in the directory: '%s'\n", f.getAbsoluteFile()));
                Arrays.stream(f.list()).forEach(s -> errors.append(s).append("\n"));
            } catch (IOException ignored) {
            }
        } catch (IOException ioe) {
            try (final var sw = new StringWriter();
                 final var pw = new PrintWriter(sw)) {
                ioe.printStackTrace(pw);
                errors.append(sw);
            } catch (IOException ignored) {
                // not happens
            }
        }
        return false;
    }

    @Override
    public String getId() {
        return "io:remove";
    }
}
