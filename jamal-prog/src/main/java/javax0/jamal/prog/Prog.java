package javax0.jamal.prog;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.prog.analyzer.Block;
import javax0.jamal.prog.analyzer.Lexer;

public class Prog implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        return Block.analyze(new Lexer().analyze(in)).execute(processor);
    }
}
