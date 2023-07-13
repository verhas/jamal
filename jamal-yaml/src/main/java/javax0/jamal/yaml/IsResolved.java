package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

public class IsResolved implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        final var id = InputHandler.fetchId(in);
        InputHandler.skipWhiteSpaces(in);
        BadSyntax.when(in.length() > 0,  "%s needs only one single argument, the name of the macro to be tested", getId());
        final var yamlObject = Resolve.getYaml(processor, id);
        return "" + yamlObject.resolved;
    }

    @Override
    public String getId() {
        return "yaml:isResolved";
    }
}
