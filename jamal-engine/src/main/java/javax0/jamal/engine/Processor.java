package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.MacroRegister;
import javax0.jamal.api.Position;
import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.tools.Marker;
import javax0.jamal.tracer.TraceRecord;
import javax0.jamal.tracer.TraceRecordFactory;

import java.util.LinkedList;
import java.util.regex.Pattern;

import static javax0.jamal.tools.InputHandler.contains;
import static javax0.jamal.tools.InputHandler.copy;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Processor implements javax0.jamal.api.Processor {

    private static final String NOT_USED = null;

    final private MacroRegister macros = new javax0.jamal.engine.macro.MacroRegister();

    final private TraceRecordFactory traceRecordFactory = new TraceRecordFactory();

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
    public UserDefinedMacro newUserDefinedMacro(String id, String input, String... params) throws BadSyntax {
        return new javax0.jamal.engine.UserDefinedMacro(this, id, input, params);
    }

    @Override
    public ScriptMacro newScriptMacro(String id, String scriptType, String input, String... params) {
        return new javax0.jamal.engine.ScriptMacro(this, id, scriptType, input, params);
    }

    @Override
    public String process(final Input in) throws BadSyntax {
        final var output = new javax0.jamal.tools.Input();
        try {
            while (in.length() > 0) {
                if (in.indexOf(macros.open()) == 0) {
                    processMacro(output, in);
                } else {
                    processText(output, in);
                }
            }
        } catch (BadSyntaxAt bsAt) {
            traceRecordFactory.dump(bsAt);
            throw bsAt;
        }
        traceRecordFactory.dump(null);
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
        try (final var tr = traceRecordFactory.openTextRecord(input.getPosition())) {
            final var nextMacroStart = input.indexOf(macros.open());
            if (nextMacroStart != -1) {
                final var text = input.substring(0, nextMacroStart);
                tr.appendResultState(text);
                output.append(text);
                skip(input, nextMacroStart);
            } else {
                output.append(input);
                input.reset();
            }
        }
    }

    /**
     * Process the macro that starts at the first character of the input.
     *
     * @param output where the processed macro is appended
     * @param input  from where the macro beforeState is read and removed
     */
    private void processMacro(Input output, Input input) throws BadSyntax {
        try (final var tr = traceRecordFactory.openMacroRecord(input.getPosition())) {
            final var macroStartPosition = input.getPosition();
            skip(input, macros.open());
            skipWhiteSpaces(input);
            final var macroRaw = getNextMacroBody(input);
            tr.appendBeforeState(macroRaw);
            final String macroProcessed;
            final var marker = new Marker("{@" + "");
            macros.push(marker);
            if (firstCharIs(macroRaw, '@')) {
                macroProcessed = macroRaw;
            } else {
                final var macroInputBefore = new javax0.jamal.tools.Input(macroRaw, macroStartPosition);
                macroProcessed = process(macroInputBefore);
                tr.appendAfterEvaluation(macroProcessed);
            }
            final var macroInputAfter = new javax0.jamal.tools.Input(macroProcessed, macroStartPosition);
            final var qualifiers = new MacroQualifier(macroInputAfter);

            final String text;
            if (qualifiers.macro instanceof InnerScopeDependent) {
                text = evalMacro(tr, qualifiers);
                macros.pop(marker);
            } else {
                macros.pop(marker);
                text = evalMacro(tr, qualifiers);
            }
            tr.appendResultState(text);
            output.append(text);
        }
    }

    /**
     * Evaluate a macro as part of the processing of it. Either user defined macro or built in.
     *
     * @param tr        trace record where the trace is sent
     * @param qualifier the qualifier that contains several parameters of the macro collected into a record
     * @return the evaluated string of the macro
     * @throws BadSyntaxAt when the syntax of the macro is bad
     */
    private String evalMacro(final TraceRecord tr,
                             final MacroQualifier qualifier) throws BadSyntaxAt {
        final var ref = qualifier.input.getPosition();
        tr.setId(qualifier.macroId);
        if (qualifier.isBuiltIn) {
            return evaluateBuiltinMacro(qualifier.input, ref, qualifier.macro);
        } else {
            tr.type(TraceRecord.Type.USER_DEFINED_MACRO);
            final String rawResult;
            try {
                rawResult = evalUserDefinedMacro(qualifier.input, tr);
                if (qualifier.isVerbatim) {
                    tr.appendAfterEvaluation(rawResult);
                    return rawResult;
                } else {
                    var result = process(new javax0.jamal.tools.Input(rawResult, qualifier.input.getPosition()));
                    tr.appendAfterEvaluation(result);
                    return result;
                }
            } catch (BadSyntaxAt bsAt) {
                throw bsAt;
            } catch (BadSyntax bs) {
                throw new BadSyntaxAt(bs, ref);
            }
        }
    }

    private String evaluateBuiltinMacro(final Input input, final Position ref, final Macro macro) throws BadSyntaxAt {
        try {
            return macro.evaluate(input, this);
        } catch (BadSyntaxAt bsAt) {
            throw bsAt;
        } catch (BadSyntax bs) {
            throw new BadSyntaxAt(bs, ref);
        }
    }

    private String evalUserDefinedMacro(final Input input, final TraceRecord tr) throws BadSyntax {
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
            parameters = input.toString().split(Pattern.quote(separator), -1);
        } else {
            parameters = new String[0];
        }
        var udMacro = macros.getUserDefined(id)
            .filter(ud -> ud instanceof Evaluable)
            .map(ud -> (Evaluable) ud);
        tr.setId(id);
        tr.setParameters(parameters);
        if (udMacro.isPresent()) {
            try {
                return udMacro.get().evaluate(parameters);
            } catch (BadSyntax bs) {
                throw bs instanceof BadSyntaxAt ? (BadSyntaxAt) bs : new BadSyntaxAt(bs, ref);
            }
        } else {
            if (reportUndef) {
                throw new BadSyntaxAt("Macro '" + id + "' is not defined.", ref);
            } else {
                return "";
            }
        }
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


    private class MacroQualifier {
        private final Input input;
        private final String macroId;
        private final boolean isVerbatim;
        private final boolean isBuiltIn;
        private final Macro macro;

        public MacroQualifier(Input input) throws BadSyntaxAt {
            this.input = input;
            final var startsAsBuiltIn = macroIsBuiltIn(input);
            if (startsAsBuiltIn) {
                skip(input, 1);
                skipWhiteSpaces(input);
                macroId = fetchId(input);
                isVerbatim = "verbatim".equals(macroId);
                isBuiltIn = !isVerbatim;
                if (isVerbatim) {
                    skipWhiteSpaces(input);
                }
            } else {
                isBuiltIn = false;
                isVerbatim = false;
                macroId = NOT_USED;
            }
            macro = getMacro(macroId, input.getPosition(), isBuiltIn);
        }
    }

    /**
     * Decides if a macro is user defined or build in. The decision is fairly simple because built in macros start with
     * {@code #} or {@code @} character.
     *
     * @param input containing the macro starting at the position zero
     * @return {@code true} if the macro is a built in macro and {@code false} if the macro is user defined
     */
    private boolean macroIsBuiltIn(Input input) {
        return input.length() > 0 && (input.charAt(0) == '#' || input.charAt(0) == '@');
    }

    /**
     * Get the macro object.
     *
     * @param macroId   the identifier of the macro.
     * @param pos       used to throw exception in case the macro is not defined
     * @param isBuiltIn signals that the macro is built-in. If the macro is not built-in the return value is {@code
     *                  null}.
     * @return the macro object or {@code null} if the macro is not built-in
     * @throws BadSyntaxAt if there is no macro registered for the given name
     */
    private Macro getMacro(String macroId, Position pos, boolean isBuiltIn) throws BadSyntaxAt {
        if (isBuiltIn) {
            var optMacro = macros.getMacro(macroId);
            if (optMacro.isEmpty()) {
                throw new BadSyntaxAt(
                    "There is no built-in macro with the id '"
                        + macroId
                        + "'", pos);
            }
            return optMacro.get();
        } else {
            return null;
        }
    }

}
