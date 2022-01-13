package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Closer;
import javax0.jamal.api.Context;
import javax0.jamal.api.Debugger;
import javax0.jamal.api.EnvironmentVariables;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static javax0.jamal.api.Macro.validIdChar;
import static javax0.jamal.api.SpecialCharacters.REPORT_UNDEFINED;
import static javax0.jamal.tools.Input.makeInput;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.getParts;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Processor implements javax0.jamal.api.Processor {

    // snipline NO_UNDEFAULT
    public static final String NO_UNDEFAULT = ":noUndefault";
    // snipline EMPTY_UNDEF
    public static final String EMPTY_UNDEF = ":emptyUndef";
    // snipline FAIL_FAST
    public static final String FAIL_FAST = ":failfast";
    // snipline LENIENT
    public static final String LENIENT = ":lenient";
    private static final String[] ZERO_STRING_ARRAY = new String[0];
    private static final Input[] ZERO_INPUT_ARRAY = new Input[0];
    final Deque<BadSyntax> exceptions = new ArrayDeque<>();
    final private MacroRegister macros = new javax0.jamal.engine.macro.MacroRegister();
    final private TraceRecordFactory traceRecordFactory = new TraceRecordFactory();
    final private StackLimiter limiter = new StackLimiter();
    final private JShellEngine shellEngine = new JShellEngine();
    final private Map<AutoCloseable, AutoCloseable> openResources = new LinkedHashMap<>();
    private final Context context;
    private final Debugger debugger;
    private final OptionsStore optionsStore;
    private boolean currentlyClosing = false;

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
        optionsStore = OptionsStore.getInstance(this);
        EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_OPTIONS_ENV).ifPresent(s -> {
            try {
                optionsStore.addOptions(getParts(makeInput(s, new Position(EnvironmentVariables.JAMAL_OPTIONS_ENV, 1, 1))));
            } catch (BadSyntaxAt e) {
                throw new IllegalArgumentException("The environment variable '"
                    + EnvironmentVariables.JAMAL_OPTIONS_ENV + "' is malformed.", e);
            }
        });
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
    public String process(final String input) throws BadSyntax {
        return process(javax0.jamal.tools.Input.makeInput(input));
    }

    @Override
    public String process(final Input input) throws BadSyntax {
        limiter.up();
        final var marker = macros.test();
        final var output = makeInput(input.getPosition());
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
        final var macroInputBefore = makeInput(macroRaw, pos.fork());
        if (firstCharIs(macroRaw, SpecialCharacters.PRE_EVALUATE)) {
            macroProcessed = process(macroInputBefore);
            tr.appendAfterEvaluation(macroProcessed);
            return macroProcessed;
        }
        tr.appendAfterEvaluation(macroRaw);
        return macroRaw;
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
        String result = safeEvaluate(() -> process(makeInput(rawResult, qualifier.input.getPosition().fork())), popper);
        final var postEvaluated = postEvaluate(result, qualifier.postEvalCount, qualifier.input.getPosition().fork());
        tr.appendAfterEvaluation(postEvaluated);
        return postEvaluated;
    }

    private String evaluateBuiltInMacro(TraceRecord tr, MacroQualifier qualifier, Runnable popper) throws BadSyntax {
        final var ref = qualifier.input.getPosition();
        tr.type(TraceRecord.Type.MACRO);
        final String result = safeEvaluate(() -> evaluateBuiltinMacro(qualifier.input, ref, qualifier.macro), popper);
        final var postEvaluated = postEvaluate(result, qualifier.postEvalCount, ref.fork());
        tr.appendAfterEvaluation(postEvaluated);
        return postEvaluated;
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
        if (optionsStore.is(FAIL_FAST)) {
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
     * then the user defined macro may not be defined. In this case the result will be an empty string. Otherwise, an
     * undefined macro results a syntax error.<p>
     *
     * @param input     starts at the start of the user defined macro but after the macro opening character and possibly
     *                  after the optional {@code @verbatim} start as well as ! and ` characters.
     * @param tr        is the tracker where the tracking information and warnings are sent
     * @param qualifier is the macro qualifying parameters
     * @return the string that is the result of the macro evaluation. If the macro is not defined, and it is preceded by
     * a {@code ?} character then the return value is am empty string.
     * @throws BadSyntax if the macro is not defined and is not preceded by a {@code ?} character or when some other
     *                   syntax error is detected.
     */
    private String evalUserDefinedMacro(final Input input, final TraceRecord tr, MacroQualifier qualifier)
        throws BadSyntax {
        var pos = input.getPosition();
        skipWhiteSpaces(input);
        final boolean reportUndefBeforeEval = doesStartWithQuestionMark(input);
        final Input evaluatedInput = evaluateMacroStart(input);
        final boolean reportUndefAfterEval = doesStartWithQuestionMark(evaluatedInput);
        final boolean reportUndef = reportUndefBeforeEval && reportUndefAfterEval && !optionsStore.is(EMPTY_UNDEF);
        skipWhiteSpaces(evaluatedInput);

        final String id = fetchId(evaluatedInput);
        qualifier.macroId = id;
        if (id.length() == 0) {
            throw new BadSyntaxAt("Zero length user defined macro name was found.", pos);
        }
        skipWhiteSpaces(evaluatedInput);
        final Optional<Identified> identifiedOpt;
        if (reportUndef || !optionsStore.is(NO_UNDEFAULT)) {
            identifiedOpt = macros.getUserDefined(id, Identified.DEFAULT_MACRO);
        } else {
            identifiedOpt = macros.getUserDefined(id);
        }
        final Optional<Evaluable> udMacroOpt = identifiedOpt
            .filter(ud -> ud instanceof Evaluable)
            .map(ud -> (Evaluable) ud);

        if (reportUndef && udMacroOpt.isEmpty()) {
            throwForUndefinedUdMacro(pos, id);
        }
        if (udMacroOpt.isPresent()) {
            qualifier.udMacro = udMacroOpt.get();
            final String[] parameters = getParameters(pos.fork(), evaluatedInput, qualifier.udMacro, id);
            tr.setId(id);
            tr.setParameters(parameters);
            try {
                qualifier.udMacro.setCurrentId(id);
                return qualifier.udMacro.evaluate(parameters);
            } catch (BadSyntax bs) {
                pushBadSyntax(bs, pos);
                return "";
            }
        } else {
            return "";
        }

    }

    /**
     * Throw an exception with an error message telling that the user defined macro was not found. While creating the
     * error message the code also checks if there is a built-in macro with the same name. In that case the error
     * message warns the user that probably the leading {@code #} or {@code @} was only missing.
     *
     * @param ref the reference to include in the exception that shows which jamal file, line and column was the error
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
            final Set<String> suggestions = getRegister().suggest(id);
            if (suggestions.isEmpty()) {
                pushBadSyntax(new BadSyntax("User defined macro '" + getRegister().open() + id + " ...' is not defined."), ref);
            } else {
                pushBadSyntax(new BadSyntax("User defined macro '" + getRegister().open() + id +
                    " ...' is not defined. Did you mean " + suggestions.stream()
                    .map(s -> "'"  + s + "'").collect(Collectors.joining(", ")) + "?"), ref);
            }
        }
    }

    /**
     * Read the input of the macro and get the parameters to pass to the macro. This is a fairly complex process that
     * follows several rules.
     * <p>
     * The method works with the partially evaluated input. If this input has no characters then the parameter array has
     * zero length.
     *
     * @param ref   the reference to the
     * @param input the partially evaluated input
     * @param macro the macro for which we evaluate the input for
     * @param id    is the original id of the macro (in case undefined and using default it may be different from
     *              what {@code macro.getId()} returns).
     * @return the parameter array
     * @throws BadSyntax if the separator character is invalid, or the evaluation of the input throws exception
     */
    private String[] getParameters(Position ref, Input input, Evaluable macro, String id) throws BadSyntax {
        final String[] parameters;
        if (input.length() > 0) {
            final var separator = input.charAt(0);
            final var expectedArgNr = macro.expectedNumberOfArguments();
            if (expectedArgNr == 0 || expectedArgNr == 1) {// note, that -1 means no limit
                if (!Character.isLetterOrDigit(separator) && input.indexOf(macros.open()) != 0) {
                    skip(input, 1);
                }
                parameters = new String[1];
                parameters[0] = process(input);
            } else {
                skip(input, 1);
                if (Character.isLetterOrDigit(separator)) {
                    throw new BadSyntaxAt("Invalid separator character '" + separator + "' ", input.getPosition());
                }
                final Input[] paramInputs = splitParameterString(input, separator);
                parameters = new String[paramInputs.length];
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = process(paramInputs[i]);
                }
            }
        } else {
            parameters = ZERO_STRING_ARRAY;
        }
        return addMacroNameForDefault(parameters, macro, id);
    }

    /**
     * If the macro is "default" and the first argument is named {@code $macro} or {@code $_} then add this extra value
     * to the start of the parameters, so that user defined {@code default} macro will know what the actual name of the
     * macro was.
     *
     * @param parameters the original parameters of the macro
     * @param macro      the macro we create the parameters for
     * @param id         the original id of the macro that was used in the source code
     * @return the original parameters array in case the macro was defined or the first argument is not {@code $macro}
     * or {@code $_}. Otherwise, the original parameter array pushed one poisition to the right and a new first parameter
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
     * @param input the input where the macro starts, or perhaps does not start, but definitely after the optional
     *              spaces
     * @return The input with the macros at the start replaced with the evaluated text of them
     * @throws BadSyntax if some macro cannot be evaluated
     */
    private Input evaluateMacroStart(Input input) throws BadSyntax {
        final Input output = makeInput("", input.getPosition().fork());
        if (input.indexOf(macros.open()) == 0) {
            while (input.length() > 0 && input.indexOf(macros.open()) == 0) {
                skip(input, macros.open());
                final var macroStart = getNextMacroBody(input);
                final var macroStartInput = makeInput(macroStart, input.getPosition().fork())
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
     * second argument contains a macro that itself has parameters, and it uses the same separator character as the top
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
     * @return the parameter array as input, with correct positioning to where the parameters start
     * @throws BadSyntaxAt if the nesting of the macro opening and closing strings do not match. The implementation does
     *                     not check this purposefully. If there are unbalanced opening and closing strings it will not
     *                     be detected.
     */
    private Input[] splitParameterString(final Input in, final char separator) throws BadSyntaxAt {
        final var open = macros.open();
        final var close = macros.close();
        final var parameters = new ArrayList<Input>();
        final var input = in.toString();
        final var pos = in.getPosition();
        int start = 0;
        int searchFrom = 0;
        while (true) {
            final var separatorIndex = input.indexOf(separator, searchFrom);
            if (separatorIndex == -1) {
                checkForImbalance(input, searchFrom, pos);
                appendTheLastParameter(parameters, input, start, pos);
                break;
            }
            final var openIndex = input.indexOf(open, searchFrom);
            final var closeIndex = input.indexOf(close, searchFrom);
            if (closeIndex < openIndex) {
                throw new BadSyntaxAt("Invalid macro nesting in the last argument of the user defined macro.",
                    pos);
            }
            if (openIndex == -1 || separatorIndex < openIndex) {
                appendTheNextParameter(parameters, input, start, separatorIndex, pos);
                start = separatorIndex + 1;
                searchFrom = start;
            } else {
                searchFrom = stepOverNestedMacros(input, openIndex, pos);
            }
        }
        return parameters.toArray(ZERO_INPUT_ARRAY);
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

    private void appendTheNextParameter(final List<Input> parameters,
                                        final String input,
                                        final int start,
                                        final int separatorIndex,
                                        final Position pos) {
        parameters.add(makeInput(input.substring(start, separatorIndex), pos.fork()));
    }

    private void appendTheLastParameter(final List<Input> parameters,
                                        final String input,
                                        final int start,
                                        final Position pos) {
        if (start < input.length()) {
            parameters.add(makeInput(input.substring(start), pos.fork()));
        } else {
            parameters.add(makeInput("", pos.fork()));
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

    public Deque<BadSyntax> errors() {
        return exceptions;
    }

    @Override
    public void throwUp() throws BadSyntax {
        throw exceptions.pop();
    }

    /**
     * This method closes the current processor invoking all the registered closers.
     * <p>
     * It ma happen that a closer is invoking the processor itself recursively and that may initiate the closing of
     * the processor recursively. In that case the original closing process should continue. To manage this situation
     * the processor state field `currentlyClosing` is set to true, meaning the closing process has already started and
     * should not be started again to avoid infinite recursion.
     * <p>
     * Closers registered during closing are ignored.
     *
     * @param result the final state of the macro processing before the closers were started.
     * @throws BadSyntax
     */
    private void closeProcess(final Input result) throws BadSyntax {
        if (currentlyClosing) {
            return;
        }
        Deque<Throwable> exceptions = new ArrayDeque<>(this.exceptions);
        final var closers = new LinkedList<>(openResources.keySet());
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
            final var nrOfExceptions = exceptions.size();
            if (nrOfExceptions == 1 && exceptions.peek() instanceof BadSyntax) {
                throw (BadSyntax) exceptions.peek();
            }
            final var sb = new StringBuilder(
                "There " + (nrOfExceptions == 1 ? "was" : "were")
                    + " " + nrOfExceptions + " syntax error" + (nrOfExceptions == 1 ? "" : "s") + " processing the Jamal input:\n");
            int exceptionSerialNum = nrOfExceptions;
            for (final var accumulated : exceptions) {
                sb.append(exceptionSerialNum--).append(". ").append(accumulated.getMessage()).append("\n");
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
    public AutoCloseable deferredClose(AutoCloseable closer) {
        if (!openResources.containsKey(closer)) {
            openResources.put(closer, closer);
        }
        return openResources.get(closer);
    }

    private interface Runnable {
        void run() throws BadSyntax;
    }

    private interface ThrowingStringSupplier {
        String get() throws BadSyntax;
    }
}
