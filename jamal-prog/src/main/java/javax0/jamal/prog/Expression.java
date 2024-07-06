package javax0.jamal.prog;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.prog.analyzer.Block;
import javax0.jamal.prog.analyzer.Lexer;
import javax0.jamal.prog.commands.Context;
import javax0.jamal.tools.Scanner;

import java.math.RoundingMode;

public class Expression implements Macro , Scanner {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var ctx = Program.getContext(processor, scanner);
        return Block.analyze(new Lexer().analyze(in)).execute(ctx);
    }

}
