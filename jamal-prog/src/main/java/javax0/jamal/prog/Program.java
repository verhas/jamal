package javax0.jamal.prog;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.prog.analyzer.Block;
import javax0.jamal.prog.analyzer.Lexer;
import javax0.jamal.prog.commands.Context;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;


public class Program implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final int stepLimit = getStepLimit(in, processor, this);
        return Block.analyze(new Lexer().analyze(in)).execute(new Context(processor, stepLimit));
    }

    static int getStepLimit(final Input in, final Processor processor, final Macro it) throws BadSyntax {
        final Params.Param<String> limit = Params.<String>holder("stepLimit").orElse("100000");
        Scan.using(processor).from(it).firstLine().keys(limit).parse(in);
        return Integer.parseInt(limit.get());
    }
}

