package javax0.jamal.py;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.Sentinel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

@Macro.Name("python")
public class PythonFunction implements Macro, Scanner {

    // snipline PYTHON_START_REGEX
    final static Pattern pattern = Pattern.compile("def\\s+(\\w[a-zA-Z0-9]*)\\s*\\(.*\\)\\s*:\\s*\n");
    public static final String VENV = "venv";

    private PythonInterpreter createInterpreter(Processor processor, String dir, String v) {
        final var interpreter = new PythonInterpreter(dir, v);
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
        // signals that the Python code is to be executed, and it does not contain any macro defining function.
        // Using this parop will simply execute the code without trying to find a macro function.
        // You cannot use this parop together with `id` or `function`.
        final var directory = scanner.str(null, "directory", "dir", "wd").defaultValue(null);
        // the working directory where the execution should start.
        // If there is a `venv`, or whatever directory name is defined in the `venv` parop under this directory,
        // then Python will run in a virtual environment.
        // This option is ignored on later calls when the interpreter was already launched.
        final var venv = scanner.str(null, "venv").defaultValue(VENV);
        // specifies the directory name of the virtual environment.
        // The default value is `venv`.
        // This option can only be used together with the option `directory`.
        final var closeCode = scanner.bool(null, "close");
        // defines that the code is to be executed when the interpreter exists.
        // The last line of the python code has to be `sys.exit(0)` or whatever exit code you desire, or else
        // the interpreter process will be killed forcefully.
        // You cannot use this parop together with `execute`, `function` or `id`.
        // When this parop is used, the return value of the method is an empty string.
        //
        //The parops are all aliases technically.
        //It means that you cannot define them as user-defined macros.
        //They have to be present on the macro between `(` and `)` characters.
        // end snippet
        scanner.done();
        BadSyntax.when(function.isPresent() && !id.isPresent(), "You cannot use '%s' unless you also specify an 'id' ", function.name());
        BadSyntax.when(venv.isPresent() && !directory.isPresent(), "venv cannot be used without specifying a directory.");
        BadSyntax.when(closeCode.is() && executeOnly.is(), "You cannot use '%s' together an '%s' command.", closeCode.name(), executeOnly.name());
        BadSyntax.when(closeCode.is() && (id.isPresent() || function.isPresent()), "You cannot use '%s' when defining a macro.", closeCode.name());
        InputHandler.skipWhiteSpaces(in);
        final var dir = directory.get();
        final var v = venv.get();
        final var sentinel = Sentinel.forThe(in).withType("python");
        BadSyntax.when(!sentinel.check(), sentinel.getErrorMessage());

        final var interp = processor.state(this, () -> createInterpreter(processor, dir, v));
        final String result;
        if (closeCode.is()) {
            interp.setCloseCode(in.toString());
            return "";
        }
        if (!executeOnly.is()) {
            final String macroName;
            final String functionName;
            if (id.isPresent()) {
                macroName = id.get();
            } else {
                final var matcher = pattern.matcher(in);
                final var found = matcher.find();
                BadSyntax.when(!found, "Macro defining method is not found");
                macroName = matcher.group(1);
            }
            if (function.isPresent()) {
                functionName = function.get();
            } else {
                functionName = macroName;
            }
            final var macro = new PythonImplementedMacro(interp, macroName, functionName);
            processor.getRegister().define(macro);
            try {
                result = interp.execute(in.toString());
            } catch (IOException e) {
                throw new BadSyntax("Error while registering python macro " + macroName, e);
            }
        } else {
            try {
                result = interp.execute(in.toString());
            } catch (IOException e) {
                throw new BadSyntax("Error while executing python code", e);
            }

        }
        return result;
    }

    private static File getRootDir(Input in) throws IOException {
        var dir = getInputFileLocation(in).getCanonicalFile();
        while (dir != null && dir.exists()) {
            final var dn = dir.getAbsolutePath();
            final var found = Arrays.stream(InputHandler.PLACEHOLDERS)
                    .filter(fn -> new File(dn, fn).exists())
                    .findFirst();
            if (found.isPresent()) {
                return dir;
            }
            dir = dir.getParentFile();
        }
        return null;
    }

    static File getInputFileLocation(Input in) {
        if (in != null && in.getPosition() != null && in.getPosition().top() != null && in.getPosition().top().file != null) {
            return new File(in.getPosition().top().file).getParentFile();
        }
        return new File(".");
    }
}
