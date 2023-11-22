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
        // snippet copy_options
        final var from = scanner.str(null, "from");
        // should specify the source file.
        // It is usually a URL starting with `https://`.
        // Note that Jamal deliberately does not support `http://` URLs.
        final var to = scanner.str(null, "to");
        // the target file name.
        // This is where the file will be saved.
        final var append = scanner.bool("io:append", "append");
        // is a boolean parameter meaning that the file should be appended.
        final var mkdir = scanner.bool("io:mkdir", "mkdir");
        // is a boolean parameter meaning that the directory where the file
        // should be saved should be created if it does not exist.
        final var useCache = scanner.bool("cache");
        //  is a boolean parameter meaning that the file download cache implemented in Jamal should be used.
        final var overwrite = scanner.bool("overwrite");
        // is a boolean parameter meaning that the file should be overwritten if it exists.
        // If this parameter is not specified and the target file exists, then the macro will not even start the copy.
        // The same effect could be reached surrounding the `io:copy` macro with an `if` macro using {%link file%} checking the existence of the target file.
        // This option is for convenience.
        // end snippet
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
