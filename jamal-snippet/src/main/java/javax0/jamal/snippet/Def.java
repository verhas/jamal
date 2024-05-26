package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Macro.Name;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import static javax0.jamal.tools.InputHandler.fetchId;

@Name("def")
public class Def implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        var id = fetchId(in);
        BadSyntax.when(id.contains(":"), "The id in the def macro cannot contain ':'");
        InputHandler.skipWhiteSpaces(in);
        BadSyntax.when(in.length() == 0 || in.charAt(0) != '=', "Missing = after the id in the def macro");
        InputHandler.skip(in, 1);
        final var value = in.toString();
        final var macro = processor.newUserDefinedMacro(id, value);
        processor.define(macro);
        return value;
    }
}
