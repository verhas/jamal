package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Closer;
import javax0.jamal.api.Context;
import javax0.jamal.api.Debugger;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.MacroRegister;
import javax0.jamal.api.Position;
import javax0.jamal.api.SpecialCharacters;
import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.engine.debugger.DebuggerFactory;
import javax0.jamal.engine.util.ExceptionDumper;
import javax0.jamal.engine.util.MacroBodyFetcher;
import javax0.jamal.engine.util.MacroQualifier;
import javax0.jamal.engine.util.PrefixComposer;
import javax0.jamal.tools.Marker;
import javax0.jamal.tools.OptionsStore;
import javax0.jamal.tracer.TraceRecord;
import javax0.jamal.tracer.TraceRecordFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static javax0.jamal.api.Macro.validIdChar;
import static javax0.jamal.api.SpecialCharacters.REPORT_UNDEFINED;
import static javax0.jamal.tools.Input.makeInput;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Processor implements javax0.jamal.api.Processor {

    private static final String[] ZERO_STRING_ARRAY = new String[0];

    final private MacroRegister macros = new javax0.jamal.engine.macro.MacroRegister();

    final private TraceRecordFactory traceRecordFactory = new TraceRecordFactory();

    final private StackLimiter limiter = new StackLimiter();

    final private JShellEngine shellEngine = new JShellEngine();

    final private Set<AutoCloseable> openResources = new LinkedHashSet<>();

    private final Context context;

    private final Debugger debugger;

    /**
     * Create a new Processor that can be used to process macros. It sets the separators to the specified values. These
     * separators start and end macros and the usual strings are "{" and "}".
     * <p>
     * The constructor also loads the macros that are defined either in the modules as implementations provided for the
     * interface {@code Macro} or in library files listed in the META-INF directory (old way). The constructor uses the
     * {@code java.util.ServiceLoader} to load the macros.
     * <p>
     * Neither {@code macroOpen} nor {@code macroClose} can be {@code null}. In case any of these parameters are {@code
     * null} an {@code IllegalArgumentException} will be thrown.
     *
     * @param macroOpen  the macro opening string
     * @param macroClose the macro closing string
     * @param context    is the embedding context
     */
    public Processor(String macroOpen, String macroClose, Context context) {
        this.context = context;
        try {
            macros.separators(macroOpen, macroClose);
        } catch (BadSyntax badSyntax) {
            throw new IllegalArgumentException(
                "neither the macroOpen nor the macroClose arguments to the constructor Processor() can be null");
        }
        Macro.getInstances().forEach(macros::define);
        debugger = DebuggerFactory.build(this);
    }

    public Processor(String macroOpen, String macroClose) {
        this(macroOpen, macroClose, null);
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
        return newUserDefinedMacro(id, input, false, params);
    }

    @Override
    public UserDefinedMacro newUserDefinedMacro(String id, String input, boolean verbatim, String... params) throws BadSyntax {
        return new javax0.jamal.engine.UserDefinedMacro(this, id, input, verbatim, params);
    }

    @Override
    public ScriptMacro newScriptMacro(String id, String scriptType, String input, String... params) throws BadSyntax {
        return new javax0.jamal.engine.ScriptMacro(this, id, scriptType, input, params);
    }

    @Override
    public String process(final Input input) throws BadSyntax {
        limiter.up();
        final var marker = macros.test();
        final var output = makeInput();
        try {
            while (input.length() > 0) {
                debugger.setBefore(limiter.get(), input);
                if (input.indexOf(macros.open()) == 0) {
                    skip(input, macros.open());
                    skipWhiteSpaces(input);
                    processMacro(input, output);
                } else {
                    processText(input, output);
                }
                debugger.setAfter(limiter.get(), output);
            }
        } catch (BadSyntaxAt bsAt) {
            traceRecordFactory.dump(bsAt);
            if (!(debugger instanceof NullDebugger)) {
                debugger.setAfter(limiter.get(), ExceptionDumper.dump(bsAt));
            }
            throw bsAt;
        } finally {
            if (limiter.down() == 0) {
                closeProcess(output);
            }
        }
        traceRecordFactory.dump(null);
        macros.test(marker);
        return output.toString();
    }

    @Override
    public MacroRegister getRegister() {
        return macros;
    }

    @Override
    public JShellEngine getJShellEngine() {
        return shellEngine;
    }

    /**
     * Process the text at the start of input till the first macro start.
     *
     * @param input  where the text is read from and removed afterwards
     * @param output where the text is appended
     */
    private void processText(Input input, Input output) {
        try (final var tr = traceRecordFactory.openTextRecord(input.getPosition())) {
            final var nextMacroStart = input.indexOf(macros.open());
            if (nextMacroStart != -1) {
                final var text = input.substring(0, nextMacroStart);
                debugger.setStart(text);
                tr.appendResultState(text);
                output.append(text);
                skip(input, nextMacroStart);
            } else {// there are no more macros on the input
                debugger.setStart(input);
                output.append(input);
                input.reset();
            }
        }
    }

    /**
     * Process the macro that starts at the first character of the input. This is already over the macro opening
     * string.
     *
     * @param input  from where the macro beforeState is read and removed
     * @param output where the processed macro is appended
     */
    private void processMacro(Input input, Input output) throws BadSyntax {
        try (final var tr = traceRecordFactory.openMacroRecord(input.getPosition())) {
            final var pos = input.getPosition();
            final var prefix = PrefixComposer.compose(input);

            if (prefix.identCount > 0) {
                outputUnevaluated(input, output, prefix);
                return;
            }
            final var position = input.getPosition();
            final var macroRaw = getNextMacroBody(input);

            final var marker = new Marker(macroRaw, position);
            macros.push(marker);
            final String macroProcessed;
            final MacroQualifier qualifiers;
            macroProcessed = getMacroPreProcessed(macroRaw, pos, tr);
            try {
                qualifiers = new MacroQualifier(this, makeInput(macroProcessed, pos), prefix.postEvalCount);
            } catch (BadSyntax bs) {
                pushBadSyntax(bs, pos);
                return;
            }

            final String text;
            if (qualifiers.isInnerScopeDependent()) {
                text = evalMacro(tr, qualifiers, () -> macros.pop(marker), this::noop);
            } else if (qualifiers.isBuiltIn) {
                BadSyntaxAt.run(() -> macros.pop(marker)).orThrowWith(qualifiers.input.getPosition());
                text = evalMacro(tr, qualifiers, this::noop, this::noop);
            } else {
                text = evalMacro(tr, qualifiers, () -> macros.pop(marker), () -> macros.lock(marker));
            }
            tr.appendResultState(text);
            output.append(text);
        }
    }

    /**
     * Processes the macro use. There are four cases:
     *
     * <ul>
     *     <li>Built-in macro starts with {@code @} character: returned as it is
     *     <li>Built-in macro starts with {@code #} character: evaluated resolving macros
     *     <li>User defined macro old style option is in effect: evaluated resolving macros
     *     <li>User defined macro no old style: returned as it is
     * </ul>
     * <p>
     * Note that the processing of the macro itself comes only after this
     *
     * @param macroRaw the raw macro that may optionally be processed
     * @param pos      the position in the input
     * @param tr       trace output
     * @return the macro use processed (or not)
     * @throws BadSyntax when there is some problem
     */
    private String getMacroPreProcessed(String macroRaw, Position pos, TraceRecord tr) throws BadSyntax {
        tr.appendBeforeState(macroRaw);
        final String macroProcessed;
        if (firstCharIs(macroRaw, SpecialCharacters.NO_PRE_EVALUATE)) {
            return macroRaw;
        }
        final var macroInputBefore = makeInput(macroRaw, pos);
        if (firstCharIs(macroRaw, SpecialCharacters.PRE_EVALUATE)) {
            macroProcessed = process(macroInputBefore);
            tr.appendAfterEvaluation(macroProcessed);
            return macroProcessed;
        }
        macroProcessed = processUdMacroOldStyleOrNone(macroRaw, macroInputBefore);
        tr.appendAfterEvaluation(macroProcessed);
        return macroProcessed;
    }

    /**
     * Output the macro that starts at the input unevaluated. The input starts after the pre-, post-evaluate prefixes.
     * At this point the input may contain some spaces and the {@code #} or {@code @} character in case of a built-in
     * macro and then the name of the macro and so on.
     * <p>
     * output will contain the whole macro, including
     * <p>
     * <ul>
     *     <li> macro opening string
     *     <li> the prefix
     *     <li> the macro content
     *     <li> the closing string
     * </ul>
     *
     * @param input  the input following the pre-, and post-evaluate prefixes
     * @param output where the macro is to output unevaluated
     * @param prefix the pre- and post-evaluate prefixes with one ident prefix less than it was in the input (when this
     *               method is called these are already consumed, the PrefixComposer consumes one ident char is there is
     *               any).
     * @throws BadSyntaxAt if the macro is not terminated before the end of the file
     */
    private void outputUnevaluated(Input input, Input output, PrefixComposer.Prefix prefix) throws BadSyntaxAt {
        output.append(getRegister().open() + prefix.string + getNextMacroBody(input) + getRegister().close());
    }

    /**
     * No operation.
     */
    private void noop() {
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
    private String processUdMacroOldStyleOrNone(String macroRaw, Input macroInputBefore)
        throws BadSyntax {
        if (option("omasalgotm").isPresent()) {
            return process(macroInputBefore);
        } else {
            return macroRaw;
        }
    }

    private interface Runnable {
        void run() throws BadSyntax;
    }

    /**
     * Evaluate a macro as part of the processing of it. Either user defined macro or built in.
     *
     * @param tr        trace record where the trace is sent
     * @param qualifier the qualifier that contains several parameters of the macro collected into a record
     * @param popper    the runnable that will pop the macro stack
     * @param locker    the runnable that will lock the current level of the macro stack
     * @return the evaluated string of the macro
     * @throws BadSyntaxAt when the syntax of the macro is bad
     */
    private String evalMacro(final TraceRecord tr, final MacroQualifier qualifier, Runnable popper, Runnable locker) throws BadSyntax {
        final var ref = qualifier.input.getPosition();
        tr.setId(qualifier.macroId);
        if (qualifier.isBuiltIn) {
            return evaluateBuiltInMacro(tr, qualifier, popper);
        } else {
            tr.type(TraceRecord.Type.USER_DEFINED_MACRO);
            final String rawResult;
            try {
                rawResult = evalUserDefinedMacro(qualifier.input, tr, qualifier);
                locker.run();
                if (qualifier.isVerbatim) {
                    if (qualifier.postEvalCount > 0) {
                        throw new BadSyntax("Verbatim and ! cannot be used together on a user defined macro.");
                    }
                    tr.appendAfterEvaluation(rawResult);
                    popper.run();
                    return rawResult;
                } else {
                    if (qualifier.udMacro != null && qualifier.udMacro.isVerbatim()) {
                        if (qualifier.postEvalCount > 0) {
                            qualifier.postEvalCount--;
                            final var result = evaluateUserDefinedMacro(rawResult, qualifier, popper, tr);
                            qualifier.postEvalCount++;
                            return result;
                        }
                        tr.appendAfterEvaluation(rawResult);
                        popper.run();
                        return rawResult;
                    } else {
                        return evaluateUserDefinedMacro(rawResult, qualifier, popper, tr);
                    }
                }
            } catch (BadSyntaxAt bsAt) {
                throw bsAt;
            } catch (BadSyntax bs) {
                throw new BadSyntaxAt(bs, ref);
            }
        }
    }

    private String evaluateUserDefinedMacro(String rawResult, MacroQualifier qualifier, Runnable popper, TraceRecord tr) throws BadSyntax {
        String result = safeEvaluate(() -> process(makeInput(rawResult, qualifier.input.getPosition())), popper);
        final var postEvaluated = postEvaluate(result, qualifier.postEvalCount, qualifier.input.getPosition());
        tr.appendAfterEvaluation(postEvaluated);
        return postEvaluated;
    }


    private String evaluateBuiltInMacro(TraceRecord tr, MacroQualifier qualifier, Runnable popper) throws BadSyntax {
        final var ref = qualifier.input.getPosition();
        tr.type(TraceRecord.Type.MACRO);
        final String result = safeEvaluate(() -> evaluateBuiltinMacro(qualifier.input, ref, qualifier.macro), popper);
        final var postEvaluated = postEvaluate(result, qualifier.postEvalCount, ref);
        tr.appendAfterEvaluation(postEvaluated);
        return postEvaluated;
    }

    private interface ThrowingStringSupplier {
        String get() throws BadSyntax;
    }

    private String safeEvaluate(ThrowingStringSupplier supplier, Runnable finalizer) throws BadSyntax {
        Exception savedEx = null;
        try {
            return supplier.get();
        } catch (Exception e) {
            savedEx = e;
            throw e;
        } finally {
            try {
                finalizer.run();
            } catch (BadSyntax unbalancedMarkers) {
                if (savedEx != null) {
                    savedEx.addSuppressed(unbalancedMarkers);
                    if (savedEx instanceof BadSyntax)
                        throw (BadSyntax) savedEx;
                    else throw new BadSyntax("There was an exception", savedEx);
                } else {
                    throw unbalancedMarkers;
                }
            }
        }
    }

    /**
     * Post evaluate built-in or user defined macro if there are {@code !} characters in the prefix.
     *
     * @param input the macroText
     * @param count the number of times the evaluation has to run
     * @param ref   the reference in case there is an error
     * @return the input evaluated postEvalCountTimes
     * @throws BadSyntax when there is some problem
     */
    private String postEvaluate(String input, int count, Position ref) throws BadSyntax {
        for (int i = 0; i < count; i++) {
            input = process(makeInput(input, ref));
        }
        return input;
    }

    private void pushBadSyntax(BadSyntax bs, final Position ref) throws BadSyntaxAt {
        final BadSyntaxAt bsa = bs instanceof BadSyntaxAt ? ((BadSyntaxAt) bs) : new BadSyntaxAt(bs, ref);
        if (option("failfast").isPresent()) {
            throw bsa;
        } else {
            exceptions.push(bsa);
        }
    }

    private String evaluateBuiltinMacro(final Input input, final Position ref, final Macro macro) throws BadSyntaxAt {
        try {
            return macro.evaluate(input, this);
        } catch (BadSyntax bs) {
            pushBadSyntax(bs, ref);
            return "";
        }
    }

    /**
     * Evaluate a user defined macro that starts at the start of the input. If it starts with a  {@code ?} character
     * then the user defined macro may not be defined. In this case the result will be an empty string. Otherwise an
     * undefined macro results a syntax error.<p>
     *
     * @param input     starts at the start of the user defined macro but after the macro opening character and possibly
     *                  after the optional {@code @verbatim} start as well as ! and ` characters.
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

        final String id = fetchId(evaluatedInput);
        qualifier.macroId = id;
        if (id.length() == 0) {
            throw new BadSyntaxAt("Zero length user defined macro name was found.", ref);
        }
        skipWhiteSpaces(evaluatedInput);

        final var udMacroOpt = macros.getUserDefined(id, Identified.DEFAULT_MACRO)
            .filter(ud -> ud instanceof Evaluable)
            .map(ud -> (Evaluable) ud);
        if (reportUndef && udMacroOpt.isEmpty()) {
            throwForUndefinedUdMacro(ref, id);
        }
        if (udMacroOpt.isPresent()) {
            qualifier.udMacro = udMacroOpt.get();
            final String[] parameters = getParameters(tr, qualifier, ref, evaluatedInput, qualifier.udMacro, id);
            tr.setId(id);
            tr.setParameters(parameters);
            try {
                qualifier.udMacro.setCurrentId(id);
                return qualifier.udMacro.evaluate(parameters);
            } catch (BadSyntax bs) {
                pushBadSyntax(bs, ref);
                return "";
            }
        } else {
            return "";
        }
    }

    /**
     * Throw an exception with an error message telling that the user defined macro was not found. While creating the
     * error message the code also checks if there is a built-in macro with the same name. In that case the error
     * message warns the user that probablythe leading {@code #} or {@code @} was only missing.
     *
     * @param ref the reference to include in the excepetion that shows which jamal file, line and column was the error
     *            at
     * @param id  the identifier of the macro that was not found
     * @throws BadSyntaxAt always, this is the main purpose of this method
     */
    private void throwForUndefinedUdMacro(Position ref, String id) throws BadSyntaxAt {
        final var optMacro = macros.getMacro(id);
        if (optMacro.isPresent()) {
            pushBadSyntax(new BadSyntax("User defined macro '" + getRegister().open() + id +
                "' is not defined. Did you want to use built-in '" + getRegister().open() + "@" + id + "' instead?"), ref);
        } else {
            pushBadSyntax(new BadSyntax("User defined macro '" + getRegister().open() + id + " ...' is not defined."), ref);
        }
    }

    /**
     * Read the input of the macro and get the parameters to pass to the macro. This is a fairly complex process that
     * follows several rules.
     * <p>
     * The input the method works with contains the already partially evaluated. If this input has no characters then
     * the parameter array has zero length. The evaluation and the input splitting also cares the {@code omasalgotm}
     * option.
     *
     * @param tr        record tracer
     * @param qualifier macro evaluation parameters
     * @param ref       the reference to
     * @param input     the partially evaluated input, depends on the option {@code omasalgotm}
     * @param macro     the macro for which we evaluate the input for
     * @param id        is the original id of the macro (in case undefined and using default it may be different from
     *                  what {@code macro.getId()} returns.
     * @return the parameter array
     * @throws BadSyntax if the separator character is invalid, or the evaluation of the input throws exception
     */
    private String[] getParameters(TraceRecord tr, MacroQualifier qualifier, Position ref, Input input, Evaluable macro, String id) throws BadSyntax {
        final String[] parameters;
        if (input.length() > 0) {
            var separator = input.charAt(0);
            if (!qualifier.oldStyle && (macro.expectedNumberOfArguments() == 0 || macro.expectedNumberOfArguments() == 1)) {
                if (!Character.isLetterOrDigit(separator) && input.indexOf(macros.open()) != 0) {
                    skip(input, 1);
                }
                parameters = new String[1];
                parameters[0] = process(input);
            } else {
                skip(input, 1);
                if (Character.isLetterOrDigit(separator)) {
                    if (qualifier.oldStyle) {
                        tr.warning("separator character '" + separator + "' is probably a mistake at " +
                            input.getPosition().file + ":" + input.getPosition().line + ":" + input.getPosition().column);
                    } else {
                        throw new BadSyntaxAt("Invalid separator character '" + separator + "' ", input.getPosition());
                    }
                }
                if (qualifier.oldStyle) {
                    parameters = input.toString().split(Pattern.quote("" + separator), -1);
                } else {
                    parameters = splitParameterString(input, separator);
                    for (int i = 0; i < parameters.length; i++) {
                        parameters[i] = process(makeInput(parameters[i], ref));
                    }
                }
            }
        } else {
            parameters = ZERO_STRING_ARRAY;
        }
        return addMacroNameForDefault(parameters, macro, id);
    }

    /**
     * If the macro is "default" and the first argument is named {@code $macro} or {@code $_} then add this extra value
     * to the start of the parameters, so that user defined {@code default} macro will know what th actual name of the
     * macro was.
     *
     * @param parameters the original parameters of the macro
     * @param macro      the macro we create the parameters for
     * @param id         the original id of the macro that was used in the source code
     * @return the original parameters array in case the macro was defined or the first argument is not {@code $macro}
     * or {@code $_}. Otherwise the original parameter array pushed one poisition to the right and a new first parameter
     * added to the string array containing the name of the original macro, which is not defined.
     */
    private String[] addMacroNameForDefault(String[] parameters, Evaluable macro, String id) {
        if (macro.getId().equals(Identified.DEFAULT_MACRO) &&
            macro instanceof javax0.jamal.engine.UserDefinedMacro) {
            final var arguments = ((javax0.jamal.engine.UserDefinedMacro) macro).getParameters();
            if (arguments.length > 0 &&
                (Identified.MACRO_NAME_ARG1.equals(arguments[0]) || Identified.MACRO_NAME_ARG2.equals(arguments[0]))) {
                final var modified = new String[parameters.length + 1];
                System.arraycopy(parameters, 0, modified, 1, parameters.length);
                modified[0] = id;
                return modified;
            }
        }
        return parameters;
    }

    /**
     * Checks if the input starts with a '{@code ?}'. If it does then it eats the character and the optionally following
     * space characters from the input and returns {@code true}.
     *
     * @param input to be checked for '{@code ?}' at the start
     * @return {@code true} if the first character is a '{@code ?}'.
     */
    private boolean doesStartWithQuestionMark(Input input) {
        final boolean reportUndefBeforeEval = !firstCharIs(input, REPORT_UNDEFINED);
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
                skip(input, macros.open());
                final var macroStart = getNextMacroBody(input);
                final var macroStartInput = makeInput(macroStart, input.getPosition())
                    .append(macros.close());
                final var macroStartOutput = makeInput();
                processMacro(macroStartInput, macroStartOutput);
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
     * @param pos    the position at the start of the input
     * @throws BadSyntaxAt when the result of the evaluation contains the separator character
     */
    private void checkEvalResultUDMacroName(Input output, Position pos) throws BadSyntaxAt {
        int i = firstCharIs(output, REPORT_UNDEFINED) ? 1 : 0;
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
     * level macro of this example. (In the example above we assumed that the macro opening and closing strings are the
     * curly braces.)<p>
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

    String getNextMacroBody(final Input input) throws BadSyntaxAt {
        final var body = MacroBodyFetcher.getNextMacroBody(input, this);
        debugger.setStart(getRegister().open() + body + getRegister().close());
        return body;
    }

    @Override
    public void close() {
        shellEngine.close();
        debugger.close();
    }

    final Deque<BadSyntax> exceptions = new ArrayDeque<>();

    public Deque<BadSyntax> errors() {
        return exceptions;
    }

    @Override
    public void throwUp() throws BadSyntax {
        throw exceptions.pop();
    }

    private boolean currentlyClosing = false;

    private void closeProcess(final Input result) throws BadSyntax {
        if( currentlyClosing ){
            return;
        }
        Deque<Throwable> exceptions = new ArrayDeque<>(this.exceptions);
        final var closers = new LinkedHashSet<AutoCloseable>();
        closers.addAll(openResources);
        try {
            currentlyClosing = true;
            for (final var resource : closers) {
                try {
                    setAwares(resource, result);
                    resource.close();
                } catch (Exception e) {
                    exceptions.push(e);
                }
            }
        } finally {
            // they were closed, they are not open anymore
            openResources.clear();
            currentlyClosing = false;
        }
        if (!exceptions.isEmpty()) {
            final var nrofExceptions = exceptions.size();
            if (nrofExceptions == 1 && exceptions.peek() instanceof BadSyntax) {
                throw (BadSyntax) exceptions.peek();
            }
            final var sb = new StringBuilder(
                "There " + (nrofExceptions == 1 ? "was" : "were")
                    + " " + nrofExceptions + " syntax error" + (nrofExceptions == 1 ? "" : "s") + " processing the Jamal input:\n");
            int ser = nrofExceptions;
            for (final var accumulated : exceptions) {
                sb.append(ser--).append(". ").append(accumulated.getMessage()).append("\n");
            }
            final var exception = new BadSyntax(sb.toString());
            for (final var accumulated : exceptions) {
                exception.addSuppressed(accumulated);
            }
            throw exception;
        }
    }

    /**
     * If the resource needs the processor instance or the output then inject these using the implemented {@link
     * javax0.jamal.api.Closer.ProcessorAware#set(javax0.jamal.api.Processor) set(T t)} injecting the output or even the
     * processor into the resource.
     *
     * @param resource that may need the processor or the output to be injected into
     * @param result   the output {@link Input} structure.
     */
    private void setAwares(AutoCloseable resource, Input result) {
        if (resource instanceof Closer.ProcessorAware) {
            ((Closer.ProcessorAware) resource).set(this);
        }
        if (resource instanceof Closer.OutputAware) {
            ((Closer.OutputAware) resource).set(result);
        }
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void deferredClose(AutoCloseable closer) {
        openResources.add(closer);
    }

    private static final Optional<Boolean> OPTIONAL_TRUE = Optional.of(true);

    /**
     * Returns an optional telling if an option is present or not.
     *
     * @param optionName the name of the option
     * @return an empty optional if the option is not present and a non-empty optional if the option is present. The
     * value inside the optional is not defined.
     */
    Optional<Boolean> option(String optionName) {
        return OptionsStore.getInstance(this).is(optionName) ? OPTIONAL_TRUE : Optional.empty();
    }

}
