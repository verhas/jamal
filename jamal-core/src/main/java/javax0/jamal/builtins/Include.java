package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Marker;
import javax0.jamal.tools.Params;

import static javax0.jamal.tools.FileTools.absolute;
import static javax0.jamal.tools.FileTools.getInput;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Include implements Macro {
    /**
     * Count the depth of the includes. In case this is more than 100 stop the processing. Most likely this is a wrong
     * recursive include that would cause stack overflow.
     */
    private int depth = getDepth();

    private static final String JAMAL_INCLUDE_DEPTH = "JAMAL_INCLUDE_DEPTH";
    private static final int DEFAULT_DEPTH = 100;

    private static int getDepth() {
        final String limitString = System.getenv(JAMAL_INCLUDE_DEPTH);
        if (limitString == null) {
            return DEFAULT_DEPTH;
        }
        try {
            return Integer.parseInt(limitString);
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(new BadSyntax("The environment variable " + JAMAL_INCLUDE_DEPTH + " should be an integer"));
        }
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var position = input.getPosition();
        final var verbatim = Params.<Boolean>holder("includeVerbatim","verbatim").asBoolean();
        Params.using(processor).from(this).startWith('(').endWith(')').keys(verbatim).parse(input);

        skipWhiteSpaces(input);
        var reference = input.getReference();
        var fileName = absolute(reference, input.toString().trim());
        if (depth-- == 0) {
            depth = getDepth(); // try macro may recover
            throw new BadSyntax("Include depth is too deep");
        }
        var marker = new Marker("{@include " + fileName + "}", position);
        final String result;
        processor.getRegister().push(marker);
        if( verbatim.get() ){
            result = getInput(fileName).toString();
        }else {
            result = processor.process(getInput(fileName));
        }
        processor.getRegister().pop(marker);
        depth++;
        return result;
    }
}
