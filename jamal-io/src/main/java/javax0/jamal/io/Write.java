package javax0.jamal.io;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class Write implements Macro, InnerScopeDependent, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var file = Utils.getFile(scanner);
        final var append = scanner.bool("io:append", "append");
        final var mkdir = scanner.bool("io:mkdir", "mkdir");
        scanner.done();

        InputHandler.skipWhiteSpaces(in);

        final var fileName = Utils.getFile(file,in);
        final var f = new File(fileName);
        if (mkdir.is()) {
            //noinspection ResultOfMethodCallIgnored
            f.getParentFile().mkdirs();
        }
        try( final var fos = new FileOutputStream(f, append.is());
             final var stream = new OutputStreamWriter(fos, StandardCharsets.UTF_8)
        ){
            stream.append(in);
        } catch (IOException ioException) {
            throw new BadSyntax("There was an IOException writing the file '"+fileName+"'",ioException);
        }

        return "";
    }

    @Override
    public String getId() {
        return "io:write";
    }
}
