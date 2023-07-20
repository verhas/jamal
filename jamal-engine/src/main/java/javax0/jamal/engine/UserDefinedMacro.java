package javax0.jamal.engine;

import javax0.jamal.api.*;
import javax0.jamal.engine.macro.ParameterSegment;
import javax0.jamal.engine.macro.Segment;
import javax0.jamal.engine.macro.TextSegment;
import javax0.jamal.engine.util.Replacer;
import javax0.jamal.engine.util.SeparatorCalculator;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.OptionsStore;
import javax0.jamal.tools.Scanner;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Stores the information about a user defined macro and can also evaluate it using actual parameter string values.
 */
public class UserDefinedMacro implements javax0.jamal.api.UserDefinedMacro, Configurable, Debuggable.UserDefinedMacro, Counted, Scanner.WholeInput {
    final private String id;
    final private boolean verbatim;
    final private boolean tailParameter;
    final private Processor processor;
    final private OptionsStore optionsStore;
    final private String content;
    final private ArgumentHandler argumentHandler;
    final private String openStr, closeStr;
    private Segment root = null;
    private boolean pure = false;

    private boolean xtended = false;
    private Map<String, String> defaults = new HashMap<>();
    private static final String ESCAPE = "@escape ";

    @Override
    public Optional<UserDefinedMacro> debuggable() {
        return Optional.of(this);
    }

    @Override
    public void configure(String key, Object object) {
        switch (key) {
            case "xtended":
                xtended = true;
                break;
            case "defaults":
                defaults.putAll(convertToMap(object.toString()));
                break;
            case "pure":
                pure = true;
                break;
            default:
                throw new IllegalArgumentException("Unknown configuration key: " + key);
        }
    }

    /**
     * Convert a multi-line string into a map. The string is a list of lines. Each line is a key value pair separated
     * by the first {@code =} character. The key is the string before the {@code =} character and the value is the
     * string after the {@code =} character. The keys are trimmed, the values are not.
     *
     * @param s the input string
     * @return the new Map
     */
    private static Map<String, String> convertToMap(String s) {
        final var map = new HashMap<String, String>();
        for (final var line : s.split("\n")) {
            final var parts = line.split("=", 2);
            if (parts.length == 2) {
                map.put(parts[0].trim(), parts[1]);
            }
        }
        return map;
    }

    /**
     * Creates a new user defined macro.
     *
     * @param processor  is the context of the evaluation. Through this object a macro can access the evaluation
     *                   environment.
     * @param id         the identifier of the macro. This is the string that stands after the {@code define} keyword
     *                   when the user defined macro is defined. This is a unique identified in the context where the
     *                   macro is reachable and usable.
     * @param content    the text of the macro that stands after the {@code =} character and before the macro closing
     *                   character.
     * @param parameters the names of the parameters. These do not actually need to be real identifiers, alphanumeric or
     *                   something like that. The only requirement is that there is no comma in these names. It is
     *                   recommended though to use usual identifiers.
     * @throws BadSyntax is thrown if one of the parameter names contain another parameter name. This would not be safe
     *                   because this way the result of the macro would be dependent on the evaluation order of the
     *                   parameters.
     */
    public UserDefinedMacro(Processor processor, String id, String content, boolean verbatim, boolean tailParameter, String... parameters) throws BadSyntax {
        this.processor = processor;
        this.optionsStore = OptionsStore.getInstance(processor);
        this.openStr = processor.getRegister().open();
        this.closeStr = processor.getRegister().close();
        this.id = id;
        this.verbatim = verbatim;
        this.tailParameter = tailParameter;
        this.content = content;
        argumentHandler = new ArgumentHandler(this, parameters);
        InputHandler.ensure(parameters, null);
    }

    public UserDefinedMacro(Processor processor, String id, String content, String... parameters) throws BadSyntax {
        this(processor, id, content, false, false, parameters);
    }

    @Override
    public boolean isVerbatim() {
        return verbatim;
    }

    /**
     * Get the name / identifier of the user defined macro.
     *
     * @return the id.
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Evaluate the content of the user-defined macro using the actual values for the parameter values.
     *
     * @param parameters the actual string values for the parameters
     * @return the string that is the result of the evaluation
     * @throws BadSyntaxAt if the user defined macro is a script and the script evaluation throws exception. This
     *                     exception is thrown if the number of the actual values is not the same as the number of the
     *                     parameters.
     */
    @Override
    public String evaluate(final String... parameters) throws BadSyntax {
        final Map<String, String> values;
        if (xtended) {
            final var scanner = newScanner(Input.makeInput(parameters.length > 0 ? parameters[0] : ""), processor);
            for (final var parameter : argumentHandler.parameters) {
                scanner.str(parameter).defaultValue(defaults.get(parameter));
            }
            scanner.done();
            values = new HashMap<>();
            for (final var param : scanner.getParMap().entrySet()) {
                final var name = param.getKey();
                final var value = param.getValue().isPresent() ? param.getValue().get().toString() : Objects.requireNonNullElse(defaults.get(name),"");
                values.put(name, value);
            }
        } else {
            final var adjustedValues = argumentHandler.adjustActualValues(parameters, optionsStore.is(Processor.LENIENT));
            values = argumentHandler.buildValueMap(adjustedValues);
        }
        if (root == null) {
            root = createSegmentList();
        }
        final var output = new StringBuilder(segmentsLengthSum(root, values));
        final String sep = pure ||
                (openStr.equals(processor.getRegister().open()) && closeStr.equals(processor.getRegister().close()))
                ? null :
                "`" + new SeparatorCalculator("abcdefghijklmnopqsrtxvyz")
                        .calculate(processor.getRegister().open() + processor.getRegister().close())
                        + "`";
        for (Segment segment = root; segment != null; segment = segment.next()) {
            if (segment instanceof ParameterSegment) {
                output.append(segment.content(values));
            } else {
                output.append(protect(segment.content(values), sep));
            }
        }
        return output.toString();
    }

    private Segment createSegmentList() {
        final Segment root = new TextSegment(null, content);
        for (int i = 0; i < argumentHandler.parameters.length; i++) {
            for (Segment segment = root; segment != null; segment = segment.next()) {
                segment.split(argumentHandler.parameters[i]);
            }
        }
        if (root.content(null).length() == 0) {
            return root.next();
        } else {
            return root;
        }
    }

    private int segmentsLengthSum(Segment root, Map<String, String> values) {
        int size = 0;
        for (Segment segment = root; segment != null; segment = segment.next()) {
            if (segment.content(values) != null) {
                size += segment.content(values).length();
            }
        }
        return size;
    }

    /**
     * Protect the input string escaping all macro start and macro end string. The same time the definition time macro
     * start and end strings are replaced to the current one.
     *
     * @param input the input string to protect
     * @param sep   the separator character to be used in the escape macro.
     * @return the protected string
     */
    private String protect(String input, String sep) {
        if (sep != null) {
            final String currOpen = processor.getRegister().open();
            final String currClose = processor.getRegister().close();
            final var replacer = new Replacer(Map.of(
                    currOpen, currOpen + ESCAPE + sep + currOpen + sep + currClose,
                    currClose, currOpen + ESCAPE + sep + currClose + sep + currClose,
                    openStr, currOpen,
                    closeStr, currClose
            ), openStr);
            return replacer.replace(input);
        } else {
            return input;
        }
    }

    /**
     * Return the number of the expected argument, as defined in the interface. This implementation makes a little
     * correction. If the macro is named "default", and the first argument is {@code $macro} or {@code $_} then it
     * returns the number of arguments minus one.
     * <p>
     * The reason for that is that the result of this method is used to count the number of the argument provided when
     * the macro is invoked. In case the macro is named {@code default} and the first argument is named as above this
     * parameter will get the name of the original macro, which was used in the Jamal source file and which was not
     * defined. When a macro is not defined, Jamal tries to call the macro named "default" and if the first argument is as
     * named above, it will insert the name of the original and undefined macro name in front of the other parameters.
     *
     * @return the number of values expected on the call of the macro.
     */
    @Override
    public int expectedNumberOfArguments() {
        if (Identified.DEFAULT_MACRO.equals(getId()) &&
                argumentHandler.parameters.length > 0 &&
                (Identified.MACRO_NAME_ARG1.equals(argumentHandler.parameters[0])
                        || Identified.MACRO_NAME_ARG2.equals(argumentHandler.parameters[0]))) {
            return argumentHandler.parameters.length - 1;
        }
        if (xtended) {
            return 1;
        } else {
            return tailParameter ? -argumentHandler.parameters.length : argumentHandler.parameters.length;
        }
    }

    @Override
    public String[] getParameters() {
        return argumentHandler.parameters;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getOpenStr() {
        return openStr;
    }


    @Override
    public String getCloseStr() {
        return closeStr;
    }

    private long usageCounter = 0;

    @Override
    public void count() {
        usageCounter++;
    }

    @Override
    public long counted() {
        return usageCounter;
    }

    private final Base64.Encoder encoder = Base64.getEncoder();
    private final Base64.Decoder decoder = Base64.getDecoder();

    private String encode(String s) {
        return encoder.encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String s) {
        return new String(decoder.decode(s), StandardCharsets.UTF_8);
    }

    private static final int ID_POS = 0;
    private static final int OPEN_STR_POS = ID_POS + 1;
    private static final int CLOSE_STR_POS = OPEN_STR_POS + 1;
    private static final int VERBATIM_POS = CLOSE_STR_POS + 1;
    private static final int TAIL_PARAMETER_POS = VERBATIM_POS + 1;
    private static final int PURE_POS = TAIL_PARAMETER_POS + 1;
    private static final int CONTENT_POS = PURE_POS + 1;
    private static final int PARAMETERS_POS = CONTENT_POS + 1;

    /**
     * Create a serialized representation of the macro.
     *
     * @param processor  the processor
     * @param serialized the serialized representation
     * @throws BadSyntax if the serialized representation is not valid
     */
    private UserDefinedMacro(Processor processor, String serialized) throws BadSyntax {
        this.processor = processor;
        this.optionsStore = OptionsStore.getInstance(processor);
        final var parts = serialized.split("\\|");
        id = decode(parts[ID_POS]);
        openStr = decode(parts[OPEN_STR_POS]);
        closeStr = decode(parts[CLOSE_STR_POS]);
        verbatim = parts[VERBATIM_POS].equals("1");
        tailParameter = parts[TAIL_PARAMETER_POS].equals("1");
        pure = parts[PURE_POS].equals("1");
        content = decode(parts[CONTENT_POS]);
        String[] parameters = new String[parts.length - PARAMETERS_POS];
        for (int i = PARAMETERS_POS; i < parts.length; i++) {
            parameters[i - PARAMETERS_POS] = decode(parts[i]);
        }
        argumentHandler = new ArgumentHandler(this, parameters);
        InputHandler.ensure(parameters, null);
    }

    @Override
    public javax0.jamal.api.UserDefinedMacro deserialize(String serialized) throws BadSyntax {
        return new javax0.jamal.engine.UserDefinedMacro(processor, serialized);
    }

    @Override
    public String serialize() {
        final var sb = new StringBuilder();
        sb.append(encode(id)).append("|");
        sb.append(encode(openStr)).append("|");
        sb.append(encode(closeStr)).append("|");
        sb.append(verbatim ? 1 : 0).append("|");
        sb.append(tailParameter ? 1 : 0).append("|");
        sb.append(pure ? 1 : 0).append("|");
        sb.append(encode(content)).append("|");
        for (final var p : argumentHandler.parameters) {
            sb.append(encode(p)).append("|");
        }
        return sb.toString();
    }

}
