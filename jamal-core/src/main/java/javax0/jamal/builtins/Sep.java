package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Parop;

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
        final var validator = Parop.validator(processor);
        trim(input);
        if (input.length() == 0) {
            restoreLastSavedDelimiters(processor);
            return "";
        }
        if (validator.when(input.length() == 1)
                .then("macro 'sep' has too short argument, only a single character")
                .hasFailed()) {
            return "";
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
            final var sep = input.substring(1, 2);
            closeMacro = input.substring(2);
            if (sep.equals(openMacro) || sep.equals(closeMacro)) {
                final var open = processor.getRegister().open();
                final var close = processor.getRegister().close();
                processor.deferredThrow("%s @sep %s%s is not correct. Use something different to separate the two characters.", open, input, close);
            }
        } else {
            final var matcher = PATTERN.matcher(input);
            if (matcher.matches()) {
                // format {@sep [% %]}
                openMacro = matcher.group(1);
                closeMacro = matcher.group(2);
                if (misleadingOpenString(openMacro, closeMacro)) {
                    final var open = processor.getRegister().open();
                    final var close = processor.getRegister().close();
                    processor.deferredThrow("%s@sep %s%s is ambiguous. Use a definition that does not contain spaces.", open, input, close);
                }
            } else {
                var sep = input.substring(0, 1);
                skip(input, 1);
                var sepIndex = input.indexOf(sep);
                if (validator.when(sepIndex == -1).then("macro 'sep' needs two separators, like {@sep/[[/]]} where '/' is the separator").hasFailed()) {
                    return "";
                }
                openMacro = input.substring(0, sepIndex).trim();
                closeMacro = input.substring(sepIndex + 1).trim();
                if (validator.when(closeMacro.contains(sep)).then("macro 'sep' closing string must not contain the separator character").hasFailed()) {
                    return "";
                }
            }
        }

        if (validator.when(openMacro.isEmpty() || closeMacro.isEmpty()).then("using macro 'sep' you cannot define zero length macro open and/or macro close strings")
                .when(openMacro.equals(closeMacro)).then("using macro 'sep' you cannot use the same string as macro opening and macro closing string").anyFailed()) {
            return "";
        }
        processor.separators(openMacro, closeMacro);
        return "";
    }

    /**
     * There is a special case that leads to unreadable {@code {@sep }} macro. This is the case when somebody uses the
     * {@code \S+\s+\S+} format but the definition also looks like the one using the separator character. In this case
     * we throw an exception.
     * <p>
     * The unreadable situation is when the opening string starts and ends with the same character, it has more than two
     * characters, and this character does not appear inside the opening string.
     * <p>
     * That way we can avoid
     * <pre>{@code
     * {@sep /[/ ]}
     * }</pre>
     * <p>
     * but still can handle safely
     *
     * <pre>{@code
     * {@sep (()) }
     * {@sep (( ())) }
     * }</pre>
     * <p>
     * For some examples you can have a look at the unit tests in {@code jamal/jamal-extensions/src/test/java/javax0/jamal/extensions/TestSep.java}
     *
     * @param openMacro    the macro opening string to test
     * @param closingMacro the macro closing string to test
     * @return true if the macro opening string suggest that the sep definition is misleading
     */
    private boolean misleadingOpenString(final String openMacro, final String closingMacro) {
        final var sep = openMacro.charAt(0);
        return (sep == openMacro.charAt(openMacro.length() - 1)
                && openMacro.length() > 2
                && !openMacro.substring(1, openMacro.length() - 1).contains("" + sep))
                ||
                (sep == closingMacro.charAt(0) &&
                        closingMacro.length() > 1 &&
                        !closingMacro.substring(1).contains("" + sep)
                )
                ;
    }

    private void restoreLastSavedDelimiters(Processor processor) throws BadSyntax {
        processor.separators(null, null);
    }
}
/*template jm_sep
{template |sep|sep $O$ $C$|define opening and closing string|
  {variable |O|"[!"}
  {variable |C|"!]"}
}
 */