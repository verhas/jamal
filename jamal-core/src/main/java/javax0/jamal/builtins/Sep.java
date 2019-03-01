package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Sep implements Macro {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        skipWhiteSpaces(input);
        if (input.length() == 0) {
            restoreLastSavedDelimiters(processor);
        } else {
            var sep = input.substring(0, 1);
            skip(input, 1);
            var sepIndex = input.indexOf(sep);
            if (sepIndex == -1) {
                throw new BadSyntax("macro 'sep' needs two separators, like {@sep/[[/]]} where '/' is the separator");
            }
            var openMacro = input.substring(0, sepIndex);
            var closeMacro = input.substring(sepIndex + 1);
            if (openMacro.length() == 0 || closeMacro.length() == 0) {
                throw new BadSyntax("using macro 'sep' you cannot define zero length macro open and/or macro close strings");
            }
            processor.separators(openMacro, closeMacro);
        }
        return "";
    }

    private void restoreLastSavedDelimiters(Processor processor) throws BadSyntax {
        processor.separators(null, null);
    }
}
