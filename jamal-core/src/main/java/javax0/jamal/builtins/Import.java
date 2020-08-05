package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Processor;
import javax0.jamal.api.Stackable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax0.jamal.tools.FileTools.absolute;
import static javax0.jamal.tools.FileTools.getInput;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Imports the macros from the file. <p> The file name is followed by the macro keyword {@code import}. If the file was
 * already imported on the actual level or some higher level then the file will not be imported again. This is to ensure
 * that the same macro file will not be imported uselessly more than once. Also if the macro file was imported on a
 * level only that is deeper than the current level then the macros are not available at the current position (except
 * those that were exported) and therefore will be imported again. <p>
 *
 * The output generated from the file is ignored and is thrown away. <p>
 *
 * The class implements Stackable. This will signal the processor to invoke {@link #push()} and {@link #pop()}<p> when
 * the macro context opens and closes one level. That way the macro can keep track of the level of already imported
 * files in it's own stack.
 */
public class Import implements Stackable {
    private final List<Set<String>> importedAlready = new ArrayList<>();

    /**
     * The constructor of this class is not implicit because it has to initialize the stack of Set that keeps tract of
     * already imported files (one element of the set for the corresponding level of the stack). When the application
     * starts the stack (implemented as an ArrayList) has to have one (empty set) element already.
     */
    public Import() {
        push();
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        skipWhiteSpaces(input);
        var reference = input.getReference();
        var fileName = absolute(reference, input.toString().trim());
        if (wasNotImported(fileName)) {
            importedAlready.get(importedAlready.size() - 1).add(fileName);
            processor.process(getInput(fileName));
        }
        return "";
    }

    /**
     *
     * Check that the file was not imported yet. In case a file was already imported then it should not be processed
     * again. A file was imported if the import macro was invoked on this level or any level higher. In these cases the
     * macros that were imported have the effect at the current level, and thus there is no reason to import the file
     * again. <p>
     *
     * If the file was imported any level deeper then the macros are already purged (import should be performed on this
     * level to have the effect, macros defined on this level) and so are the elements of the list that keeps track of
     * the files imported on each level.<p>
     *
     * @param fileName the absolute name of the file to be imported now.
     * @return if the file was not imported yet.
     */
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
