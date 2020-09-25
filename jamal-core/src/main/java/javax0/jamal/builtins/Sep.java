package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.util.regex.Pattern;

import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.trim;

/**
 * This macro can be used to redefine the macro start and end string.
 */
public class Sep implements Macro {

    private static final Pattern PATTERN = Pattern.compile("(\\S+)\\s+(\\S+)");

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        trim(input);
        if (input.length() == 0) {
            restoreLastSavedDelimiters(processor);
            return "";
        }

        if (input.length() == 1) {
            throw new BadSyntax("macro 'sep' has too short argument, only a singl character");
        }

        final String openMacro;
        final String closeMacro;
        if (input.length() == 2) {
            // format {@sep []}
            openMacro = input.substring(0, 1);
            closeMacro = input.substring(1);
        } else if (input.length() == 3) {
            // format {@sep [/]}
            openMacro = input.substring(0, 1);
            closeMacro = input.substring(2);
            final var sep = input.substring(1, 2);
            if (sep.equals(openMacro) || sep.equals(closeMacro)) {
                final var open = processor.getRegister().open();
                final var close = processor.getRegister().close();
                throw new BadSyntax(open + "@sep " + input + close + " is not correct. Use something different to separate the two characters.");
            }
        } else {
            final var matcher = PATTERN.matcher(input);
            if (matcher.matches()) {
                openMacro = matcher.group(1);
                closeMacro = matcher.group(2);
            } else {
                var sep = input.substring(0, 1);
                skip(input, 1);
                var sepIndex = input.indexOf(sep);
                if (sepIndex == -1) {
                    throw new BadSyntax("macro 'sep' needs two separators, like {@sep/[[/]]} where '/' is the separator");
                }
                openMacro = input.substring(0, sepIndex).trim();
                closeMacro = input.substring(sepIndex + 1).trim();
                if (closeMacro.contains(sep)) {
                    throw new BadSyntax("macro 'sep' closing string must not contain the separator character");
                }
            }
        }

        if (openMacro.length() == 0 || closeMacro.length() == 0) {
            throw new BadSyntax("using macro 'sep' you cannot define zero length macro open and/or macro close strings");
        }
        if (openMacro.equals(closeMacro)) {
            throw new BadSyntax("using macro 'sep' you cannot use the same string as macro opening and macro closing string");
        }
        processor.separators(openMacro, closeMacro);
        return "";
    }

    private void restoreLastSavedDelimiters(Processor processor) throws BadSyntax {
        processor.separators(null, null);
    }
}
