package javax0.jamal.ruby;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Cast;
import javax0.jamal.tools.InputHandler;

public class RubyProperty implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var shell = Shell.getShell(processor);
        InputHandler.skipWhiteSpaces(in);
        final var id = InputHandler.fetchId(in);
        InputHandler.skipWhiteSpaces(in);
        if (in.length() == 0) {
            return "" + shell.property(id);
        }
        if (InputHandler.firstCharIs(in, '=')) {
            InputHandler.skip(in, 1);
        } else {
            throw new BadSyntax("There must be a '=' after the name of the Ruby property to assign a value to it.");
        }
        InputHandler.skipWhiteSpaces(in);
        shell.property(id, Cast.cast(in.toString()));
        return "";
    }

    @Override
    public String getId() {
        return "ruby:property";
    }
}
