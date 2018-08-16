package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Macro;
import javax0.jamal.api.MacroRegister;
import javax0.jamal.api.UserDefinedMacro;

import java.util.regex.Pattern;

import static javax0.jamal.tools.InputHandler.*;

public class Processor implements javax0.jamal.api.Processor {

    final private MacroRegister macros = new javax0.jamal.engine.macro.MacroRegister();
    private final String macroOpen;
    private final String macroClose;

    public Processor(String macroOpen, String macroClose) {
        this.macroOpen = macroOpen;
        this.macroClose = macroClose;
        Macro.getInstances()
            .forEach(macros::put);
    }

    @Override
    public UserDefinedMacro newUserDefinedMacro(String id, String input, String[] params) throws BadSyntax {
        return new javax0.jamal.engine.UserDefinedMacro(id, input, params);
    }

    @Override
    public String process(final String in) throws BadSyntax {
        final var output = new StringBuilder();
        final var input = new StringBuilder(in);
        while (input.length() > 0) {
            if (input.indexOf(macroOpen) == 0) {
                processMacro(output, input);
            } else {
                processText(output, input);
            }
        }
        return output.toString();
    }

    @Override
    public MacroRegister getRegister() {
        return macros;
    }

    /**
     * Process the text at the start of input till the first macro start.
     *
     * @param output where the text is appended
     * @param input  where the text is read from and removed after wards
     */
    private void processText(StringBuilder output, StringBuilder input) {
        int nextMacroStart = input.indexOf(macroOpen);
        if (-1 < nextMacroStart) {
            output.append(input.substring(0, nextMacroStart));
            skip(input, nextMacroStart);
        } else {
            output.append(input);
            input.setLength(0);
        }
    }

    /**
     * Process the macro that starts at the first character of the input.
     *
     * @param output where the processed macro is appended
     * @param input  from where the macro source is read and removed
     */
    private void processMacro(StringBuilder output, StringBuilder input) throws BadSyntax {
        skip(input, macroOpen);
        skipWhiteSpaces(input);
        var macro = getNextMacroBody(input);
        if (!firstCharIs(macro, '@')) {
            macros.push();
            macro = process(macro);
            macros.pop();
        }
        output.append(evalMacro(macro));
    }


    /**
     * Evaluate a macro. Either user defined macro, built in or otherwise defined.
     *
     * @param macro the macro text to be processed without the opening and closing string.
     * @return the evaluated macro
     */
    String evalMacro(final String macro) throws BadSyntax {
        final var input = new StringBuilder(macro);
        var isBuiltin = macroIsBuiltIn(input);
        final String macroId;
        final boolean verbatim;
        if (isBuiltin) {
            skip(input, 1);
            skipWhiteSpaces(input);
            var fetched = fetchId(input);
            verbatim = "verbatim".equals(fetched);
            if (verbatim) {
                skipWhiteSpaces(input);
                isBuiltin = macroIsBuiltIn(input);
                if (isBuiltin) {
                    macroId = fetchId(input);
                } else {
                    macroId = fetched;
                }
            } else {
                macroId = fetched;
            }
        } else {
            verbatim = false;
            macroId = null;
        }
        if (isBuiltin) {
            var builtin = macros.geMacro(macroId);
            if (!builtin.isPresent()) {
                throw new BadSyntax();
            }
            return builtin.get().evaluate(input, this);
        } else {
            var rawResult = evalUserDefinedMacro(input);
            return verbatim ? rawResult : process(rawResult);
        }
    }

    private String evalUserDefinedMacro(final StringBuilder input) throws BadSyntax {
        final boolean reportUndef = input.charAt(0) == '?';
        if (reportUndef) {
            skip(input, 1);
        }
        skipWhiteSpaces(input);
        var id = fetchId(input);
            if (id.length() == 0) {
                return "";
            }
        if (id.charAt(0) == '$') {
            //TODO handle loop variables
            return "";
        } else {
            skipWhiteSpaces(input);
            final String[] parameters;
            if (input.length() > 0) {
                var separator = input.substring(0, 1);
                skip(input, 1);
                parameters = input.toString().split(Pattern.quote(separator));
            } else {
                parameters = new String[0];
            }
            var udMacro = macros.getUserMacro(id);
            if (udMacro.isPresent() && reportUndef) {
                throw new BadSyntax();
            }
            if (udMacro.isPresent()) {
                return udMacro.get().evaluate(parameters);
            } else {
                return "";
            }
        }
    }

    /**
     * decides if a macro is user defined or build in. The decision is fairly simple
     * because built in macros start with {@code #} or {@code @} character.
     *
     * @param input containing the macro starting at the position zero
     * @return {@code true} if the macro is a built in macro and {@code false} if the macro is user defined
     */
    private boolean macroIsBuiltIn(StringBuilder input) {
        return input.charAt(0) == '#' || input.charAt(0) == '@';
    }


    String getNextMacroBody(final StringBuilder input) {

        var counter = 1; // we are after one macro opening, so that counts as one opening
        final var output = new StringBuilder();

        while (counter > 0) {// while there is any opened macro
            if (input.length() == 0) {// some macro was not closed
                //&Error('Erroneous macro nesting.',$output);
                return output.toString();
            }

            if (input.indexOf(macroOpen) == 0) {
                moveMacroOpenToOutput(input, output);
                counter++; //count the new opening
            } else if (input.indexOf(macroClose) == 0) {
                counter--; // count the closing
                if (counter == 0) {
                    skip(input, macroClose);
                    return output.toString();
                } else {
                    moveMacroCloseToOutput(input, output);
                }
            } else {
                var open = input.indexOf(macroOpen);
                var close = input.indexOf(macroClose);
                if (contains(close) && (!contains(open) || close < open)) {
                    open = close;
                }
                if (!contains(open)) {
                    output.append(input);
                    input.setLength(0);
                } else {
                    output.append(input.substring(0, open));
                    skip(input, open);
                }
            }
        }
        return output.toString();
    }

    private void moveMacroCloseToOutput(StringBuilder input, StringBuilder output) {
        copy(input, output, macroClose);
    }

    private void moveMacroOpenToOutput(StringBuilder input, StringBuilder output) {
        copy(input, output, macroOpen);
    }


}
