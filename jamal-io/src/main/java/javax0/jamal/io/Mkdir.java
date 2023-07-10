package javax0.jamal.io;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;

import java.io.File;

public class Mkdir implements Macro, InnerScopeDependent, Scanner.FirstLine {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var file = Utils.getFile(scanner);
        final var recursive = Utils.getRecursive(scanner);
        scanner.done();

        final var fileName = Utils.getFile(file, in);

        final boolean done;
        if (recursive.is()) {
            done = new File(fileName).mkdirs();
        } else {
            done = new File(fileName).mkdir();
        }
        BadSyntax.when(!done, "Directory '%s' cannot be created", fileName);
        return "";
    }

    @Override
    public String getId() {
        return "io:mkdir";
    }
}
