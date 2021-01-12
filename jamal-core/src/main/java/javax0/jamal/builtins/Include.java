package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Marker;

import static javax0.jamal.tools.FileTools.absolute;
import static javax0.jamal.tools.FileTools.getInput;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Include implements Macro {
    /**
     * Count the depth of the includes. In case this is more than 100 stop the processing. Most likely this is a wrong
     * recursive include that would cause stack overflow.
     */
    private int depth = 100;

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        skipWhiteSpaces(input);
        var reference = input.getReference();
        if (reference == null) {

        }
        var fileName = absolute(reference, input.toString().trim());
        if (depth-- == 0) {
            throw new BadSyntax("Include depth is too deep");
        }
        var marker = new Marker("{@include " + fileName + "}");
        final String result;
        try {
            processor.getRegister().push(marker);
            result = processor.process(getInput(fileName));
        } finally {
            processor.getRegister().pop(marker);
        }
        depth++;
        return result;
    }
}
