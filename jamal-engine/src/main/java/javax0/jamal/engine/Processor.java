package javax0.jamal.engine;

import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.api.*;
import javax0.jamal.tools.Marker;

import java.util.LinkedList;
import java.util.regex.Pattern;

import static javax0.jamal.tools.InputHandler.*;

public class Processor implements javax0.jamal.api.Processor {

    private static final String NOT_USED = null;
    final private MacroRegister macros = new javax0.jamal.engine.macro.MacroRegister();

    /**
     * Create a new Processor that can be used to process macros. It sets the separators to the specified values.
     * These separators start and end macros and the usual strings are "{" and "}".
     * <p>
     * The constructor also loads the macros that are defined either in the modules as implementations provided for the
     * interface {@link Macro} or in library files listed in the META-INF directory (old way). The constructor uses the
     * {@link java.util.ServiceLoader} to load the macros.
     * <p>
     * Neither {@code macroOpen} nor {@code macroClose} can be {@code null}. In case any of these parameters are
     * {@code null} an {@link IllegalArgumentException} will be thrown.
     *
     * @param macroOpen  the macro opening string
     * @param macroClose the macro closing string
     */
    public Processor(String macroOpen, String macroClose) {
        try {
            macros.separators(macroOpen, macroClose);
        } catch (BadSyntax badSyntax) {
            throw new IllegalArgumentException(
                "neither the macroOpen nor the macroClose arguments to the constructor Processor() can be null");
        }
        Macro.getInstances().forEach(macros::define);
    }

    /**
     * Complimentary constructor that creates a processor with the conventional separators: "{" and "}".
     * <p>
     * Note that any string containing many characters can be used as separators. It is recommended to use different
     * strings as opening and closing string or else it will not be possible to nest macros into each other.
     */
    public Processor() {
        this("{", "}");
    }

    @Override
    public UserDefinedMacro newUserDefinedMacro(String id, String input, String[] params) throws BadSyntax {
        return new javax0.jamal.engine.UserDefinedMacro(id, input, params);
    }

    @Override
    public String process(final Input in) throws BadSyntaxAt, BadSyntax {
        final var output = new javax0.jamal.tools.Input();
        while (in.length() > 0) {
            if (in.indexOf(macros.open()) == 0) {
                processMacro(output, in);
            } else {
                processText(output, in);
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
    private void processText(Input output, Input input) {
        var nextMacroStart = input.indexOf(macros.open());
        if (nextMacroStart != -1) {
            output.append(input.substring(0, nextMacroStart));
            skip(input, nextMacroStart);
        } else {
            output.append(input);
            input.reset();
        }
    }

    /**
     * Process the macro that starts at the first character of the input.
     *
     * @param output where the processed macro is appended
     * @param input  from where the macro source is read and removed
     */
    private void processMacro(Input output, Input input) throws BadSyntax, BadSyntaxAt {
        final var macroStartPosition = input.getPosition();
        skip(input, macros.open());
        skipWhiteSpaces(input);
        final var macroRaw = getNextMacroBody(input);
        final String macroProcessed;
        if (!firstCharIs(macroRaw, '@')) {
            var marker = new Marker("{@" + "");
            final var macroInputBefore = new javax0.jamal.tools.Input(macroRaw,macroStartPosition);
            macros.push(marker);
            macroProcessed = process(macroInputBefore);
            macros.pop(marker);
        }else{
            macroProcessed = macroRaw;
        }
        final var macroInputAfter = new javax0.jamal.tools.Input(macroProcessed,macroStartPosition);
        output.append(evalMacro(macroInputAfter));
    }

    /**
     * Evaluate a macro. Either user defined macro or built in.
     *
     * @param input the macro text to be processed without the opening and closing string.
     * @return the evaluated macro
     */
    private String evalMacro(final Input input) throws BadSyntaxAt {
        var ref = input.getPosition();
        var isBuiltin = macroIsBuiltIn(input);
        final String macroId;
        final boolean verbatim;
        if (isBuiltin) {
            skip(input, 1);
            skipWhiteSpaces(input);
            macroId = fetchId(input);
            verbatim = "verbatim".equals(macroId);
            if (verbatim) {
                isBuiltin = false;
                skipWhiteSpaces(input);
            }
        } else {
            verbatim = false;
            macroId = NOT_USED;
        }
        if (isBuiltin) {
            var builtin = macros.getMacro(macroId);
            if (builtin.isEmpty()) {
                throw new BadSyntaxAt("There is no built-in macro with the id '" + macroId + "'", ref);
            }
            try {
                return builtin.get().evaluate(input, this);
            } catch (BadSyntax bs) {
                throw new BadSyntaxAt(bs, ref);
            }
        } else {
            final String rawResult;
            try {
                rawResult = evalUserDefinedMacro(input);
                return verbatim ?
                    rawResult :
                    process(new javax0.jamal.tools.Input(rawResult, input.getPosition()));
            } catch (BadSyntax bs) {
                throw new BadSyntaxAt(bs, ref);
            }
        }
    }

    private String evalUserDefinedMacro(final Input input) throws BadSyntax, BadSyntaxAt {
        var ref = input.getPosition();
        final boolean reportUndef = input.length() == 0 || input.charAt(0) != '?';
        if (!reportUndef) {
            skip(input, 1);
        }
        skipWhiteSpaces(input);
        var id = fetchId(input);
        if (id.length() == 0) {
            throw new BadSyntaxAt("Zero length user defined macro name was found.", ref);
        }
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
        if (udMacro.isPresent()) {
            try {
                return udMacro.get().evaluate(parameters);
            } catch (BadSyntax bs) {
                throw new BadSyntaxAt(bs.getMessage(), ref);
            }
        } else {
            if (reportUndef) {
                throw new BadSyntaxAt("Macro '" + id + "' is not defined.", ref);
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
    private boolean macroIsBuiltIn(Input input) {
        return input.length() > 0 && (input.charAt(0) == '#' || input.charAt(0) == '@');
    }


    String getNextMacroBody(final Input input) throws BadSyntaxAt {
        var refStack = new LinkedList<Position>();
        refStack.add(input.getPosition());
        var counter = 1; // we are after one macro opening, so that counts as one opening
        final var output = new javax0.jamal.tools.Input();

        while (counter > 0) {// while there is any opened macro
            if (input.length() == 0) {// some macro was not closed
                throw new BadSyntaxAt("Macro was not terminated in the file.", refStack.pop());
            }

            if (input.indexOf(macros.open()) == 0) {
                moveMacroOpenToOutput(input, output);
                refStack.add(input.getPosition());
                counter++; //count the new opening
            } else if (input.indexOf(macros.close()) == 0) {
                counter--; // count the closing
                if (counter == 0) {
                    skip(input, macros.close());
                    return output.toString();
                } else {
                    refStack.pop();
                    moveMacroCloseToOutput(input, output);
                }
            } else {
                var open = input.indexOf(macros.open());
                var close = input.indexOf(macros.close());
                if (contains(close) && (!contains(open) || close < open)) {
                    open = close;
                }
                if (!contains(open)) {
                    output.append(input);
                    input.reset();
                } else {
                    output.append(input.substring(0, open));
                    skip(input, open);
                }
            }
        }
        return output.toString();
    }

    private void moveMacroCloseToOutput(Input input, Input output) {
        copy(input, output, macros.close());
    }

    private void moveMacroOpenToOutput(Input input, Input output) {
        copy(input, output, macros.open());
    }


}
