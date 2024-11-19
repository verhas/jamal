package javax0.jamal.io;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

import java.io.PrintStream;

public class Print implements Macro, InnerScopeDependent, Scanner {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var err = scanner.bool("io:err", "err");
        scanner.done();

        final PrintStream out;
        if (err.is()) {
            out = System.err;
        } else {
            out = System.out;
        }
        InputHandler.skipWhiteSpaces(in);
        out.print(in);
        return "";
    }

    @Override
    public String getId() {
        return "io:print";
    }
}
