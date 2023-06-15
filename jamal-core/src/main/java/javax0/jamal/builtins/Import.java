package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.api.Stackable;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax0.jamal.api.SpecialCharacters.IMPORT_CLOSE;
import static javax0.jamal.api.SpecialCharacters.IMPORT_OPEN;
import static javax0.jamal.api.SpecialCharacters.IMPORT_SHEBANG1;
import static javax0.jamal.api.SpecialCharacters.IMPORT_SHEBANG2;
import static javax0.jamal.tools.FileTools.absolute;
import static javax0.jamal.tools.FileTools.getInput;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Imports the macros from the file.
 * <p>
 * The file name is following the macro keyword {@code import}. If the file was already imported on the actual level or
 * some higher level then the file will not be imported again. This is to ensure that the same macro file will not be
 * imported uselessly more than once. Also if the macro file was imported on a level only that is deeper than the
 * current level then the macros are not available at the current position (except those that were exported) and
 * therefore will be imported again.
 * <p>
 * The output generated from the file is ignored and is thrown away.
 * <p>
 * The class implements {@link Stackable}. This will signal the processor to invoke {@link #push()} and {@link #pop()}
 * <p>
 * when the macro context opens and closes one level. That way the macro can keep track of the level of already imported
 * files in it's own stack.
 * <p>
 * If the imported file starts with the characters { and @ then the import will set the macro opening and closing
 * strings to { and } for the time of the import. This way a macro file or an application can freely use any macro
 * opening and closing string and still can import macros defined in a web resource or in a JAR resource. The author of
 * a JAR file or a macro package published on the net cannot know what macro strings the user will use. It is
 * recommended to use { and @, and then the import is seamless.
 * <p>
 * Prior to version 1.5.0 importing such a file needed, assuming that {@code ((} and {@code ))} are the opening and
 * closing macro strings:
 *
 * <pre>{@code
 * ((@sep { }))
 * {@import re:my_resource.jim}
 * {@sep}
 * }</pre>
 * <p>
 * Using 1.5.0 or later the code is
 * <pre>{@code
 * ((@import re:my_resource.jim))
 * }</pre>
 */
@Macro.Stateful
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
        var position = input.getPosition();
        final var top = Params.<Boolean>holder(null, "top").asBoolean();
        final var noCache = Params.<Boolean>holder(null, "noCache").asBoolean();
        Scan.using(processor).from(this).between("[]").keys(top, noCache).parse(input);
        if (top.is()) {
            while (position.parent != null) {
                position = position.parent;
            }
        }
        skipWhiteSpaces(input);
        var reference = position.file;
        var fileName = absolute(reference, input.toString().trim());
        if (wasNotImported(fileName)) {
            importedAlready.get(importedAlready.size() - 1).add(fileName);
            final var in = getInput(fileName, position, noCache.is(), processor);
            final var weArePseudoDefault = processor.getRegister().open().equals("{") && processor.getRegister().close().equals("}");
            final var useDefaultSeparators = in.length() > 1 && in.charAt(0) == IMPORT_SHEBANG1 && in.charAt(1) == IMPORT_SHEBANG2 && !weArePseudoDefault;
            final var marker = processor.getRegister().test();
            if (useDefaultSeparators) {
                processor.separators(IMPORT_OPEN, IMPORT_CLOSE);
                processor.process(in);
                processor.separators(null, null);
            } else {
                processor.process(in);
            }
            processor.getRegister().test(marker);
        }
        return "";
    }

    /**
     * Check that the file was not imported yet. In case a file was already imported then it should not be processed
     * again. A file was imported if the import macro was invoked on this level or any level higher. In these cases the
     * macros that were imported have the effect at the current level, and thus there is no reason to import the file
     * again. <p>
     * <p>
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
