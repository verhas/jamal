package javax0.jamal.prog;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.prog.analyzer.Block;
import javax0.jamal.prog.analyzer.Lexer;
import javax0.jamal.prog.commands.Context;
import javax0.jamal.tools.Scanner;


public class Program implements Macro, Scanner.FirstLine {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final int stepLimit = getStepLimit(in, processor, this);
        return Block.analyze(new Lexer().analyze(in)).execute(new Context(processor, stepLimit));
    }

    static int getStepLimit(final Input in, final Processor processor, final Scanner.FirstLine it) throws BadSyntax {
        final var scanner = it.newScanner(in, processor);
        final var limit = scanner.number("stepLimit").defaultValue(100_000);
        scanner.done();
        return limit.get();
    }

    @Override
    public String[] getIds() {
        return new String[]{"program", "prog", "do", "run"};
    }
}

