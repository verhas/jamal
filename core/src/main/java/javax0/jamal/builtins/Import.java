package javax0.jamal.builtins;

import javax0.jamal.api.*;

import java.util.*;

import static javax0.jamal.tools.FileTools.absolute;
import static javax0.jamal.tools.FileTools.getInput;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Imports the macros from the file.
 *
 * The file name os followed by the macro keyword {@code import}. If the file was already
 * imported on the actual level or some higher level then the file will not be imported
 * again. This is to ensure that the same macro file will not be imported uselessly more
 * than once. Also if the macro file was imported on a level that is deeper than the current
 * level then the macros are not available at the current position (except those that were
 * exported) and therefore in that case the file will be imported again.
 *
 * The output generated from the file is ignored and is thrown away.
 *
 */
public class Import implements Stackable {
    private final List<Set<String>> importedAlready = new ArrayList<>();

    {
        push();
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        var input = in.getInput();
        skipWhiteSpaces(input);
        var reference = in.getReference();
        var fileName = absolute(reference, input.toString().trim());
        if (wasNotImported(fileName)) {
            importedAlready.get(importedAlready.size()-1).add(fileName);
            var ignored = processor.process(getInput(fileName));
        }
        return "";
    }

    private boolean wasNotImported(String fileName) {
        for (int level = importedAlready.size() - 1; level > -1; level--) {
            var importSet = importedAlready.get(level);
            if (importSet.contains(fileName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void push() {
        importedAlready.add(new HashSet<>());
    }

    @Override
    public void pop() {
        importedAlready.remove(importedAlready.size() - 1);
    }
}
