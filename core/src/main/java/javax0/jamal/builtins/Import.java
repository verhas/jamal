package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.util.HashSet;
import java.util.Set;

import static javax0.jamal.tools.FileTools.absolute;
import static javax0.jamal.tools.FileTools.getInput;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Import implements Macro {
    private final Set<String> importedAlready = new HashSet<>();

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        var input = in.getInput();
        skipWhiteSpaces(input);
        var reference = in.getReference();
        var fileName = absolute(reference, input.toString().trim());
        if (!importedAlready.contains(fileName)) {
            importedAlready.add(fileName);
            var ignored = processor.process(getInput(fileName));
        }
        return "";
    }
}
