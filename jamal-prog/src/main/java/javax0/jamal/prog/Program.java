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

@Macro.Name({"program", "prog", "do", "run"})
public class Program implements Macro, Scanner.FirstLine {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var ctx = getContext(processor, scanner);
        return Block.analyze(new Lexer().analyze(in)).execute(ctx);
    }

    static Context getContext(Processor processor, ScannerObject scanner) throws BadSyntax {
        final var limit = scanner.number("stepLimit").defaultValue(100_000);
        final var floating = scanner.bool("float");
        final var scale = scanner.number("scale").defaultValue(2);
        final var roundingMode = scanner.enumeration(RoundingMode.class).defaultValue(RoundingMode.HALF_UP);
        scanner.done();
        final var ctx = new Context(processor, limit.get(), floating.is(), roundingMode.get(RoundingMode.class), scale.get());
        return ctx;
    }
}

