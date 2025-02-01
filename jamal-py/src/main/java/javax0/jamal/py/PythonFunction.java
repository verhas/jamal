package javax0.jamal.py;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

import java.io.IOException;
import java.util.regex.Pattern;

@Macro.Name("python")
public class PythonFunction implements Macro, Scanner {

    // snipline PYTHON_START_REGEX
    final static Pattern pattern = Pattern.compile("def\\s+(\\w[a-zA-Z0-9]*)\\s*\\(.*\\)\\s*:\\s*\n");

    private PythonInterpreter createInterpreter(Processor processor) {
        final var interpreter = new PythonInterpreter();
        processor.deferredClose(interpreter);
        return interpreter;
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // snippet PYTHON_PAROPS
        final var id = scanner.str(null, "id", "name", "macro").defaultValue(null);
        // defines the name of the macro.
        // If this is not defined, the macro will try to use pattern matching, finding the `def XXXX(txt):` structure in the code.
        final var function = scanner.str(null, "function", "def").defaultValue(null);
        // defines the name of the function if it is different from the name of the macro.
        // It is an error using this parop without using `id` (or one of its aliases).
        final var executeOnly = scanner.bool(null, "execute");
        //
        //
        //The parops are all aliases technically.
        //It means that you cannot define them as user-defined macros.
        //They have to be present on the macro between `(` and `)` characters.
        // end snippet
        scanner.done();
        BadSyntax.when(function.isPresent() && !id.isPresent(), "You cannot use '%s' unless you also specify an 'id' ", function.name());
        InputHandler.skipWhiteSpaces(in);
        final String macroName;
        if (id.isPresent()) {
            macroName = id.get();
        } else {
            final var matcher = pattern.matcher(in);
            final var found = matcher.find();
            BadSyntax.when(!found, "Macro defining method is not found");
            macroName = matcher.group(1);
        }
        final String functionName;
        if (function.isPresent()) {
            functionName = function.get();
        } else {
            functionName = macroName;
        }
        final var interp = processor.state(this, () -> createInterpreter(processor));
        final var macro = new PythonMacro(interp, macroName, functionName);
        processor.getRegister().define(macro);

        try {
            interp.execute(in.toString());
        } catch (IOException e) {
            throw new BadSyntax("Error while registering python macro " + macroName, e);
        }

        return "";
    }
}
