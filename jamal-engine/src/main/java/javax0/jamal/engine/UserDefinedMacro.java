package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.engine.macro.Segment;
import javax0.jamal.engine.macro.TextSegment;
import javax0.jamal.engine.util.SeparatorCalculator;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.OptionsStore;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Stores the information about a user defined macro and can also evaluate it using actual parameter string values.
 */
public class UserDefinedMacro implements javax0.jamal.api.UserDefinedMacro {
    final private String id;
    final private Processor processor;
    final private String content;
    final private ArgumentHandler argumentHandler;
    final private String openStr, closeStr, pattern;
    private Segment root = null;

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
    public UserDefinedMacro(Processor processor, String id, String content, String... parameters) throws BadSyntax {
        this.processor = processor;
        this.openStr = processor.getRegister().open();
        this.closeStr = processor.getRegister().close();
        pattern = "(" + Pattern.quote(openStr) + "|" + Pattern.quote(closeStr) + ")";
        this.id = id;
        this.content = content;
        argumentHandler = new ArgumentHandler(this, parameters);
        InputHandler.ensure(parameters, null);
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


    private boolean isLenient() {
        return processor.option("lenient").isPresent();
    }

    /**
     * Evaluate the content of the user defined macro using the actual values for the parameter values.
     *
     * @param parameters the actual string values for the parameters
     * @return the string that is the result of the evaluation
     * @throws BadSyntaxAt if the user defined macro is a script and the script evaluation throws exception. This
     *                     exception is thrown if the number of the actual values is not the same as the number of the
     *                     parameters.
     */
    @Override
    public String evaluate(final String... parameters) throws BadSyntax {
        final var adjustedValues = argumentHandler.adjustActualValues(parameters, isLenient());
        var values = argumentHandler.buildValueMap(adjustedValues);
        if (root == null) {
            root = createSegmentList();
        }
        final var output = new StringBuilder(segmentsLengthSum(root, values));
        final String sep = OptionsStore.getInstance(processor).is("omasalgotm") ||
            (openStr.equals(processor.getRegister().open()) && closeStr.equals(processor.getRegister().close()))
            ? null :
            "`" + new SeparatorCalculator("abcdefghijklmnopqsrtxvyz")
                .calculate(processor.getRegister().open() + processor.getRegister().close())
                + "`";
        for (Segment segment = root; segment != null; segment = segment.next()) {
            output.append(protect(segment.content(values), sep));
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
            size += segment.content(values).length();
        }
        return size;
    }

    private String protect(String in, String sep) {
        if (sep != null) {
            final String currOpen = processor.getRegister().open();
            final String currClose = processor.getRegister().close();

            final var sb = new StringBuilder(in);
            int i = 0;
            while (i < sb.length()) {
                final int oIndex = sb.indexOf(currOpen, i);
                final int cIndex = sb.indexOf(currClose, i);
                if (oIndex == -1 && cIndex == -1) {
                    break;
                }
                if (oIndex > -1 && (oIndex < cIndex || cIndex == -1)) {
                    sb.replace(oIndex, oIndex + currOpen.length(), currOpen + "@escape " + sep + currOpen + sep + currClose);
                    i = oIndex + 2 * currOpen.length() + currClose.length() + 2 * sep.length() + "@escape ".length();
                } else {
                    sb.replace(cIndex, cIndex + currClose.length(), currOpen + "@escape " + sep + currClose + sep + currClose);
                    i = cIndex + currOpen.length() + 2 * currClose.length() + 2 * sep.length() + "@escape ".length();
                }
            }

            i = 0;
            while (i < sb.length()) {
                final int oIndex = sb.indexOf(openStr, i);
                final int cIndex = sb.indexOf(closeStr, i);
                if (oIndex == -1 && cIndex == -1) {
                    break;
                }
                if (oIndex > -1 && (oIndex < cIndex || cIndex == -1)) {
                    sb.replace(oIndex, oIndex + openStr.length(), currOpen);
                    i = oIndex + currOpen.length();
                } else {
                    sb.replace(cIndex, cIndex + closeStr.length(), currClose);
                    i = cIndex + currClose.length();
                }
            }
            return sb.toString();
        } else {
            return in;
        }
    }

    @Override
    public int expectedNumberOfArguments() {
        return argumentHandler.parameters.length;
    }

}
