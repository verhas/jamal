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
    private int depth = 100;

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        var input = in.getInput();
        skipWhiteSpaces(input);
        var reference = in.getReference();
        var fileName = absolute(reference, input.toString().trim());
        if (depth-- == 0) {
            throw new BadSyntax("Include depth is too deep");
        }
        var marker = new Marker("{@include " + fileName + "}");
        processor.getRegister().push(marker);
        var result = processor.process(getInput(fileName));
        processor.getRegister().pop(marker);
        return result;
    }
}
