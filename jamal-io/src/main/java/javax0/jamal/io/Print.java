package javax0.jamal.io;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;

import java.io.PrintStream;

public class Print implements Macro, InnerScopeDependent {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var err = Params.holder("io:err", "err").asBoolean();
        Params.using(processor).from(this).keys(err).between("()").parse(in);

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
