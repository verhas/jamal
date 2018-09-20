package javax0.jamal.builtins;

import javax0.jamal.api.*;

import java.util.ArrayList;
import java.util.List;

import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Sep implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        var input = in.getInput();
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
            processor.separators(openMacro, closeMacro);
        }
        return "";
    }

    private void restoreLastSavedDelimiters(Processor processor) throws BadSyntax {
        processor.separators(null, null);
    }
}
