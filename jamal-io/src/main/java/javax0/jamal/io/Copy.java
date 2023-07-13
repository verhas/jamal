package javax0.jamal.io;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Copy implements Macro, Scanner.WholeInput {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var from = scanner.str(null, "from");
        final var to = scanner.str(null, "to");
        final var append = scanner.bool("io:append", "append");
        final var mkdir = scanner.bool("io:mkdir", "mkdir");
        final var useCache = scanner.bool("cache");
        final var overwrite = scanner.bool("overwrite");
        scanner.done();

        final var toName = Utils.getFile(to, in);
        final var fromName = Utils.getFile(from, in);
        final var f = new File(toName);
        if (f.exists() && !overwrite.is()) {
            return "";
        }
        if (mkdir.is()) {
            //noinspection ResultOfMethodCallIgnored
            f.getParentFile().mkdirs();
        }

        try (final var fos = new FileOutputStream(f, append.is())) {
            final var bytes = FileTools.getFileBinaryContent(fromName, !useCache.is(), processor);
            fos.write(bytes);
        } catch (IOException ioException) {
            throw new BadSyntax(String.format("There was an IOException copying the file '%s' to '%s'", fromName, toName), ioException);
        }
        return "";
    }

    @Override
    public String getId() {
        return "io:copy";
    }
}
