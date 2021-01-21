package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.MacroReader;

import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class CounterMacro implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var reader = MacroReader.macro(processor);
        final var format = reader.readValue("format").orElse("%d");
        final int start = reader.integer().readValue("start").orElse(1);
        final int step = reader.integer().readValue("step").orElse(1);

        skipWhiteSpaces(input);
        var id = fetchId(input);
        skipWhiteSpaces(input);
        if( input.length() > 0 ){
            throw new BadSyntaxAt("There are extra characters after the counter definition",input.getPosition());
        }

        final Counter counter;
        if (isGlobalMacro(id)) {
            counter = new Counter(convertGlobal(id),start,step,format);
            processor.defineGlobal(counter);
        } else {
            counter = new Counter(id,start,step,format);
            processor.define(counter);
            // it has to be exported because it is inner scope dependent
            processor.getRegister().export(counter.getId());
        }
        return "";
    }

    @Override
    public String getId() {
        return "counter:define";
    }
}
