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
import javax0.jamal.tools.OptionsStore;
import javax0.jamal.tracer.TraceRecord;
import javax0.jamal.tracer.TraceRecordFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static javax0.jamal.tools.Input.makeInput;
import static javax0.jamal.tools.InputHandler.contains;
import static javax0.jamal.tools.InputHandler.copy;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.InputHandler.validIdChar;

public class Processor implements javax0.jamal.api.Processor {

    private static final String NOT_USED = null;

    private static final String[] ZERO_STRING_ARRAY = new String[0];

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
        final var output = makeInput();
        try {
            while (in.length() > 0) {
                if (in.indexOf(macros.open()) == 0) {
                    skip(in, macros.open());
                    skipWhiteSpaces(in);
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
            final var macroRaw = getNextMacroBody(input);
            tr.appendBeforeState(macroRaw);
            final String macroProcessed;
            final var marker = new Marker("{@" + "");
            macros.push(marker);
            if (firstCharIs(macroRaw, '@')) {
                macroProcessed = macroRaw;
            } else if (firstCharIs(macroRaw, '#')) {
                final var macroInputBefore = makeInput(macroRaw, macroStartPosition);
                macroProcessed = process(macroInputBefore);
                tr.appendAfterEvaluation(macroProcessed);
            } else {
                final var macroInputBefore = makeInput(macroRaw, macroStartPosition);
                macroProcessed = processMacroContentBeforeMacroItself(macroRaw, macroInputBefore);
                tr.appendAfterEvaluation(macroProcessed);
            }
            final var macroInputAfter = makeInput(macroProcessed, macroStartPosition);
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
     * Process the text of the user defined macro before the macro itself is evaluated. In case the evaluation is
     * oldStyle (option omasalgotm is defined) then the content IS evaluated. Otherwise the content is not evaluated
     * here.
     *
     * @param macroRaw         the raw macro to be evaluated
     * @param macroInputBefore the macro content as an Input object
     * @return the result after the macro body was (or was not) evaluated.
     * @throws BadSyntax if the content of the macro cannot be evaluated
     */
    private String processMacroContentBeforeMacroItself(String macroRaw, Input macroInputBefore)
        throws BadSyntax {
        if (OptionsStore.getInstance(this).is("omasalgotm")) {
            return process(macroInputBefore);
        } else {
            return macroRaw;
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
                rawResult = evalUserDefinedMacro(qualifier.input, tr, qualifier);
                if (qualifier.isVerbatim) {
                    tr.appendAfterEvaluation(rawResult);
                    return rawResult;
                } else {
                    var result = process(makeInput(rawResult, qualifier.input.getPosition()));
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

    /**
     * Evaluate a user defined macro that starts at the start of the input. If it starts with a  {@code ?} character
     * then the user defined macro may not be defined. In this case the result will be an empty string. Otherwise an
     * undefined macro results a syntax error.<p>
     *
     * @param input     starts at the pstart of the user defined macro but after the macro opening character and
     *                  possibly after the optional {@code @verbatim} start.
     * @param tr        is the tracker where the tracking information and warnings are sent
     * @param qualifier is the macro qualifying parameters
     * @return the string that is the result of the macro evaluation. If the macro is not defined and it is preceded by
     * a {@code ?} character then the return value is am empty string.
     * @throws BadSyntax if the macro is not defined and is not preceded by a {@code ?} character or when the some other
     *                   syntax error is detected.
     */
    private String evalUserDefinedMacro(final Input input, final TraceRecord tr, MacroQualifier qualifier)
        throws BadSyntax {
        var ref = input.getPosition();
        skipWhiteSpaces(input);
        final boolean reportUndefBeforeEval = doesStartWithQuestionMark(input);
        final Input evaluatedInput = evaluateMacroStart(input, qualifier);
        final boolean reportUndefAfterEval = doesStartWithQuestionMark(evaluatedInput);
        final boolean reportUndef = reportUndefBeforeEval && reportUndefAfterEval;
        skipWhiteSpaces(evaluatedInput);

        var id = fetchId(evaluatedInput);
        if (id.length() == 0) {
            throw new BadSyntaxAt("Zero length user defined macro name was found.", ref);
        }
        skipWhiteSpaces(evaluatedInput);

        final var udMacroOpt = macros.getUserDefined(id)
            .filter(ud -> ud instanceof Evaluable)
            .map(ud -> (Evaluable) ud);
        if (reportUndef && udMacroOpt.isEmpty()) {
            throw new BadSyntaxAt("Macro '" + id + "' is not defined.", ref);
        }
        if (udMacroOpt.isPresent()) {
            final var udMacro = udMacroOpt.get();
            final String[] parameters;
            if (evaluatedInput.length() > 0) {
                var separator = evaluatedInput.charAt(0);
                if (!qualifier.oldStyle && udMacro.expectedNumberOfArguments() == 1) {
                    if (!Character.isLetterOrDigit(separator) && evaluatedInput.indexOf(macros.open()) != 0) {
                        skip(evaluatedInput, 1);
                    }
                    parameters = new String[1];
                    parameters[0] = evaluatedInput.toString();
                } else {
                    skip(evaluatedInput, 1);
                    if (Character.isLetterOrDigit(separator)) {
                        if (qualifier.oldStyle) {
                            tr.warning("separator character '" + separator + "' is probably a mistake at " +
                                evaluatedInput.getPosition().file + ":" + evaluatedInput.getPosition().line + ":" + evaluatedInput.getPosition().column);
                        } else {
                            throw new BadSyntaxAt("Invalid separator character '" + separator + "' ", evaluatedInput.getPosition());
                        }
                    }
                    if (qualifier.oldStyle) {
                        parameters = evaluatedInput.toString().split(Pattern.quote("" + separator), -1);
                    } else {
                        parameters = splitParameterString(evaluatedInput, separator);
                        for (int i = 0; i < parameters.length; i++) {
                            parameters[i] = process(makeInput(parameters[i], ref));
                        }
                    }
                }
            } else {
                parameters = ZERO_STRING_ARRAY;
            }

            tr.setId(id);
            tr.setParameters(parameters);
            try {
                return udMacro.evaluate(parameters);
            } catch (BadSyntaxAt bsAt) {
                throw bsAt;
            } catch (BadSyntax bs) {
                throw new BadSyntaxAt(bs, ref);
            }
        } else {
            return "";
        }
    }

    /**
     * Checks if the input starts with a '{@code ?}'. If it does then it eats the character and the optionally following
     * space characters from the input and returns {@code true}.
     *
     * @param input to be checked for '{@code ?}' at the start
     * @return {@code true} if the first character is a '{@code ?}'.
     */
    private boolean doesStartWithQuestionMark(Input input) {
        final boolean reportUndefBeforeEval = input.length() == 0 || input.charAt(0) != '?';
        if (!reportUndefBeforeEval) {
            skip(input, 1);
            skipWhiteSpaces(input);
        }
        return reportUndefBeforeEval;
    }

    /**
     * If the start of the content itself starts with a macro then it has to be evaluated to allow constructs like
     *
     * <pre>{@code
     *    [[@define macroName=zz]]
     *    [[@define zz=hh]]
     *    [[   [[macroName]] ]]
     * }</pre>
     * <p>
     * to get {@code hh}. For the example above the {@code input} parameter will be
     *
     * <pre>{@code
     * [[macroName]] ]]
     * ^--------------^
     * }</pre>
     * <p>
     * and NOT
     *
     * <pre>{@code
     *    [[macroName]] ]]
     * ^-----------------^
     * }</pre>
     * <p>
     * The input starts after the optional spaces following the starting {@code [[} macro opening.
     * <p>
     * Note that at the start of the macro the name may be the result of the concatenation of several macros.
     *
     * @param input     the input where the macro starts, or perhaps does not start, but definitely after the optional
     *                  spaces
     * @param qualifier the macro qualifiers. It is used to know if the evaluation is oldStyle or not. If it is old
     *                  style then the macros here were already evaluated.
     * @return The input with the macros at the start replaced with the evaluated text of them
     * @throws BadSyntax if some macro cannot be evaluated
     */
    private Input evaluateMacroStart(Input input, MacroQualifier qualifier) throws BadSyntax {
        final Input output = makeInput("", input.getPosition());
        if (input.indexOf(macros.open()) == 0 && !qualifier.oldStyle) {
            while (input.length() > 0 && input.indexOf(macros.open()) == 0) {
                final var pos = input.getPosition();
                skip(input, macros.open());
                final var macroStart = getNextMacroBody(input);
                final var macroStartInput = makeInput(macroStart, input.getPosition())
                    .append(macros.close());
                final var macroStartOutput = makeInput();
                processMacro(macroStartOutput, macroStartInput);
                output.append(macroStartOutput);
            }
            skipWhiteSpaces(output);
            checkEvalResultUDMacroName(output, input.getPosition());
            return output.append(input);
        } else {
            return input;
        }
    }

    /**
     * Checks that the user defined macro name, which is the result of macro evaluation does not contain the separator
     * character.
     *
     * @param output the output that we check
     * @param pos the position at the start of the input
     * @throws BadSyntaxAt when the result of the evaluation contains the separator character
     */
    private void checkEvalResultUDMacroName(Input output, Position pos) throws BadSyntaxAt {
        int i = output.charAt(0) == '?' ? 1 : 0;
        while (i < output.length() && Character.isWhitespace(output.charAt(i))) {
            i++;
        }
        while (i < output.length() && validIdChar(output.charAt(i))) {
            i++;
        }
        if (i < output.length() && !Character.isWhitespace(output.charAt(i))) {
            throw new BadSyntaxAt("Macro evaluated result user defined macro name contains the separator. Must not.",
                pos);
        }
    }

    /**
     * Split the input into parameter strings. Does not modify the input.
     * <p>
     * <p>
     * Splitting cares about macro nesting. If the separator character appears inside a macro then it is not considered
     * as a separator character. For example:
     *
     * <pre>{@code
     *  {mySpecialMacro /this is the firstArgument/{thisIsAnotherMacro/with its/ownArguments} and the result of it is
     *         the second parameter with this string/this is the third parameter/}
     * }</pre>
     * <p>
     * In the example above the macro will have three arguments, and it is not a problem (since version 1.2.0) that the
     * second argument contains a macro that itself has parameters and it uses the same separator character as the top
     * level macro of this example. (In the example above we assumed that the macro opening and closing characters are
     * the curly braces.)<p>
     * <p>
     * NOTE: that versions prior 1.2.0 were splitting the example above into five arguments. Although there is a
     * possible use of that kind of macros this was never recommended or meant that way and because there is only a
     * narrow user base of Jamal there is no backward compatibility way of operation. If you happen to face problem
     * because of that then stay with version prior 1.2.0, e.g.: 1.1.0 and migrate your macros so that they do not use
     * tricks.
     *
     * @param in        the input that starts after the first occurrence of the separator character
     * @param separator the separator character
     * @return the parameter array
     * @throws BadSyntaxAt if the nesting of the macro opening and closing strings do not match. The implementation does
     *                     not check this purposefully. If there is an balance mismatch of opening and closing strings
     *                     then this will not be detected.
     */
    private String[] splitParameterString(final Input in, final char separator) throws BadSyntaxAt {
        final var open = macros.open();
        final var close = macros.close();
        final var parameters = new ArrayList<String>();
        final var input = in.toString();
        final var pos = in.getPosition();
        int start = 0;
        int searchFrom = 0;
        while (true) {
            final var separatorIndex = input.indexOf(separator, searchFrom);
            if (separatorIndex == -1) {
                checkForImbalance(input, searchFrom, pos);
                appendTheLastParameter(parameters, input, start);
                break;
            }
            final var openIndex = input.indexOf(open, searchFrom);
            final var closeIndex = input.indexOf(close, searchFrom);
            if (closeIndex < openIndex) {
                throw new BadSyntaxAt("Invalid macro nesting in the last argument of the user defined macro.",
                    pos);
            }
            if (openIndex == -1 || separatorIndex < openIndex) {
                appendTheNextParameter(parameters, input, start, separatorIndex);
                start = separatorIndex + 1;
                searchFrom = start;
            } else {
                searchFrom = stepOverNestedMacros(input, openIndex, pos);
            }
        }
        return parameters.toArray(ZERO_STRING_ARRAY);
    }

    /**
     * Check if there are the same number of opening and closing macro strings in the last argument of the user defined
     * macro.
     *
     * @param input      the body of the user defined macro
     * @param searchFrom the position where the last argument starts
     * @param pos        the location in the input to be used for exception construction in case there is an imbalance
     * @throws BadSyntaxAt if there are different number of macro opening and closing strings
     */
    private void checkForImbalance(final String input,
                                   final int searchFrom,
                                   final Position pos) throws BadSyntaxAt {
        final var close = macros.close();
        final var open = macros.open();
        var openIndex = input.indexOf(open, searchFrom);
        var closeIndex = input.indexOf(close, searchFrom);
        if (openIndex != -1) {
            if (closeIndex < openIndex) {
                throw new BadSyntaxAt("Invalid macro nesting in the last argument of the user defined macro.",
                    pos);
            }
            while (true) {
                openIndex = stepOverNestedMacros(input, openIndex, pos);
                if (openIndex < input.length()) {
                    break;
                }
                closeIndex = input.indexOf(close, openIndex);
                openIndex = input.indexOf(open, openIndex);
                if (openIndex == -1) {
                    if (closeIndex != -1) {
                        throw new BadSyntaxAt(
                            "There are trailing macro closing strings in the last argument of the user defined macro.",
                            pos);
                    }
                    break;
                }
            }
        } else {
            if (input.indexOf(closeIndex, searchFrom) != -1) {
                throw new BadSyntaxAt("Invalid macro nesting in the last argument of the user defined macro.",
                    pos);
            }
        }
    }

    private void appendTheNextParameter(final List<String> parameters,
                                        final String input,
                                        final int start,
                                        final int separatorIndex) {
        parameters.add(input.substring(start, separatorIndex));
    }

    private void appendTheLastParameter(final List<String> parameters,
                                        final String input,
                                        final int start) {
        if (start < input.length()) {
            parameters.add(input.substring(start));
        } else {
            parameters.add("");
        }
    }

    /**
     * Step over the macro considering also macro nesting. The return value is the index in the string that is after the
     * macro that starts at the position {@code start}.
     *
     * @param input the input that contains the macro of which we search then ending.
     * @param start the position (or before) where the first macro opening string starts
     * @param pos   position of the string in the macro file, used to compose exception message
     * @return the character position after the matching macro close string
     * @throws BadSyntaxAt if there are more macro opening than closing strings, which means an error in macro nesting
     */
    private int stepOverNestedMacros(final String input, final int start, final Position pos) throws BadSyntaxAt {
        String open = macros.open();
        String close = macros.close();
        int searchFrom = start + open.length();
        int depth = 1;
        while (true) {
            final int openIndex = input.indexOf(open, searchFrom);
            final int closeIndex = input.indexOf(close, searchFrom);
            if (openIndex == -1 && closeIndex == -1) {
                throw new BadSyntaxAt("Invalid macro nesting in the argument of the user defined macro." + input, pos);
            }
            if (openIndex == -1 || closeIndex < openIndex) {
                searchFrom = closeIndex + close.length();
                depth--;
                if (depth == 0) {
                    break;
                }
            } else {
                searchFrom = openIndex + open.length();
                depth++;
            }
        }
        return searchFrom;
    }

    /**
     * Eat off a macro from the imput caring about all the macro nesting. The input is right after the macro opening
     * string and at the end it will eat off from the input not only the macro body but also the last macro closing
     * strings. The output will be the string that contains the content of the macro including all the matching macro
     * opening and closing strings that are inside.
     * <p>
     * For example the input (here {@code [[} is the macro opening string and {@code ]]} the macro closing string):
     *
     * <pre>{@code
     *     @define z=[[userDef/indef/macro]] some content]]after it is
     *     ^---------------------------------------------------------^
     * }</pre>
     * <p>
     * (see that there is no {@code [[} at the start) will return
     *
     * <pre>{@code
     *     @define z=[[userDef/indef/macro]] some content
     *     ^--------------------------------------------^
     * }</pre>
     * <p>
     * and the input will contain the remaining
     *
     * <pre>{@code
     *     after it is
     *     ^---------^
     * }</pre>
     * <p>
     * (The {@code ^---^} shows where the strings start and end.)
     *
     * @param input the input after the macro opening string
     * @return the output that contains the body of the macro
     * @throws BadSyntaxAt if the macro opening and closing strings are not properly balanced
     */
    String getNextMacroBody(final Input input) throws BadSyntaxAt {
        var refStack = new LinkedList<Position>();
        refStack.add(input.getPosition());
        var counter = 1; // we are after one macro opening, so that counts as one opening
        final var output = makeInput();

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
                final var open = input.indexOf(macros.open());
                final var close = input.indexOf(macros.close());
                final int limit;
                if (contains(close) && (!contains(open) || close < open)) {
                    limit = close;
                } else {
                    limit = open;
                }
                if (!contains(limit)) {
                    output.append(input);
                    input.reset();
                } else {
                    output.append(input.substring(0, limit));
                    skip(input, limit);
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
        private final boolean oldStyle;

        public MacroQualifier(Input input) throws BadSyntaxAt {
            this.input = input;
            this.oldStyle = OptionsStore.getInstance(Processor.this).is("omasalgotm");
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
