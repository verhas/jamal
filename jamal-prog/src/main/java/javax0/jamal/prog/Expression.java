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

public class Expression implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        return Block.analyze(new Lexer().analyze(in)).execute(new Context(processor));
    }

}
