package javax0.jamal.builtins;

import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.tools.InputHandler.*;

public class Undefine implements Macro {

    @Override
    public String evaluate(Input input, Processor processor) {
        skipWhiteSpaces(input);

        final var id = fetchId(input);
        final var convertedId = convertGlobal(id);
        if (isGlobalMacro(id)) {
            processor.defineGlobal(new Identified.Undefined(convertedId));
        } else {
            processor.define(new Identified.Undefined(convertedId));
        }
        return "";
    }
}
/*template jm_undefine
{template |undefine|undefine $C$|undefine a macro|
  {variable |C|"macro_name"}
}
 */