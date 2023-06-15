package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.getParameters;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * See the documentation of the "for" loop in the README.doc in the project root.
 */
@Macro.Stateful
public class For implements Macro, InnerScopeDependent {

    Params.Param<String> separator;
    Params.Param<String> subSeparator;
    Params.Param<Boolean> trim;
    Params.Param<Boolean> skipEmpty;
    Params.Param<Boolean> lenient;
    Params.Param<Boolean> evalValueList;
    Processor processor;

    private enum KeyWord {
        IN, FROM
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        Position pos = input.getPosition();
        final var it = new For();
        it.processor = processor;
        it.separator = Params.<String>holder("$forsep", "separator", "sep").orElse(",");
        it.subSeparator = Params.<String>holder("$forsubsep", "subseparator", "subsep").orElse("\\|");
        it.trim = Params.<Boolean>holder("trimForValues", "trim").asBoolean();
        it.skipEmpty = Params.<Boolean>holder("skipForEmpty", "skipEmpty").asBoolean();
        it.lenient = Params.<Boolean>holder("lenient").asBoolean();
        it.evalValueList = Params.<Boolean>holder("evaluateValueList", "evalist").asBoolean();
        Scan.using(processor).from(this).between("[]").keys(it.subSeparator, it.separator, it.trim, it.skipEmpty, it.lenient, it.evalValueList).parse(input);

        skipWhiteSpaces(input);

        final String[] variables = getVariables(input);
        skipWhiteSpaces(input);
        final String[][] valueMatrix;
        switch (checkKeyword(input)) {
            case IN:
                valueMatrix = it.getValueMatrix(input, variables, pos);
                break;
            case FROM:
                final var source = fetchId(input);
                final var sourceObject = processor.getRegister().getUserDefined(source)
                        .filter(m -> m instanceof ObjectHolder<?>)
                        .map(m -> (ObjectHolder<?>) m)
                        .map(ObjectHolder::getObject)
                        .orElseThrow(() -> new BadSyntax(format("The user defined macro '%s' does not exist or cannot be used as data source for a 'for' loop.", source)));
                valueMatrix = it.getValueMatrix(sourceObject, variables.length);
                break;
            default:
                throw new IllegalArgumentException("Unknown keyword following the 'for'");
        }
        skipWhiteSpaces(input);
        checkEqualSign(input);
        final var content = input.toString();
        final var output = new StringBuilder();
        final Segment root = splitContentToSegments(variables, content);
        final var parameterMap = new HashMap<String, String>();
        for (final String[] values : valueMatrix) {
            if (values != null) {
                for (int i = 0; i < variables.length; i++) {
                    parameterMap.put(variables[i], i < values.length ? values[i] : "");
                }
                for (Segment segment = root; segment != null; segment = segment.next()) {
                    output.append(segment.content(parameterMap));
                }
            }
        }
        return output.toString();

    }

    private String[][] getValueMatrix(Object object, int nrOfVariables) {
        List<?> valueListList = convertObjectToListList(object, nrOfVariables);
        final String[][] result = new String[valueListList.size()][];
        for (int i = 0; i < result.length; i++) {
            final var valueList = convertObjectToStringList(valueListList.get(i), nrOfVariables);
            while (valueList.size() < nrOfVariables) {
                valueList.add("");
            }
            result[i] = valueList.toArray(String[]::new);
        }
        return result;
    }

    private List<String> convertObjectToStringList(Object object, int nrOfVariables) {
        final var list = new ArrayList<String>();
        for (final var v : convertObjectToListList(object, nrOfVariables)) {
            list.add(v.toString());
        }
        return list;
    }

    /**
     * Convert an object to a String. Null is converted to empty string.
     *
     * @param object to be converted
     * @return the converted String
     */
    private static String nullToEmpty(Object object) {
        if (object == null) {
            return "";
        } else {
            return "" + object;
        }
    }

    private List<?> convertObjectToListList(Object object, int nrOfVariables) {
        List<?> valueList;

        if (object instanceof Object[]) {
            valueList = List.of(((Object[]) object));
        } else if (object instanceof Set<?>) {
            valueList = new ArrayList<>(List.of(((Set<?>) object).toArray()));
        } else if (object instanceof List<?>) {
            valueList = new ArrayList<>((List<?>) object);
        } else if (object instanceof Stream<?>) {
            valueList = ((Stream<?>) object).collect(Collectors.toList());
        } else if (object instanceof Map<?, ?>) {
            if (nrOfVariables > 1) {
                valueList = map2KeyValueLists((Map<?, ?>) object);
            } else {
                valueList = new ArrayList<>(List.of(((Map<?, ?>) object).keySet().toArray()));
            }
        } else {
            valueList = new ArrayList<>(List.of("" + object));
        }
        return valueList;
    }

    private List<?> map2KeyValueLists(Map<?, ?> map) {
        List<?> valueList;
        valueList = map.entrySet()
                .stream()
                .map(e -> List.of(nullToEmpty(e.getKey()), nullToEmpty(e.getValue())))
                .collect(Collectors.toList());
        return valueList;
    }

    private String[][] getValueMatrix(Input input, String[] variables, Position pos) throws BadSyntax {
        if (firstCharIs(input, '(')) {
            return createValueMatrixFromString(getValuesStringFromSimpleList(input), variables, pos);
        } else if (firstCharIs(input, '`')) {
            return createValueMatrixFromString(getValuesStringFromStringTerminatedList(input), variables, pos);
        } else {
            throw new BadSyntaxAt("for macro has bad syntax '" + input + "'", input.getPosition());
        }
    }

    private String[][] createValueMatrixFromString(String valuesString,
                                                   String[] variables,
                                                   Position pos) throws BadSyntax {
        if (this.evalValueList.is()) {
            valuesString = processor.process(javax0.jamal.tools.Input.makeInput(valuesString, pos));
        }
        final String[] valueArray = valuesString.split(separator.get(), -1);

        final String[][] valueMatrix = new String[valueArray.length][];
        for (int j = 0; j < valueArray.length; j++) {
            final var value = valueArray[j];
            if (value.length() > 0 || !skipEmpty.is()) {
                final String[] values = value.split(subSeparator.get(), -1);
                BadSyntax.when(!lenient.is() && values.length != variables.length, () -> String.format("number of the values does not match the number of the parameters\n%s\n%s",
                                String.join(",", variables), value));
                if (trim.is()) {
                    for (int i = 0; i < values.length; i++) {
                        values[i] = values[i].trim();
                    }
                }
                valueMatrix[j] = values;
            }
        }
        return valueMatrix;
    }

    /**
     * Split the content into segments. Each segment is either content text or a variable reference in the text. These
     * segments follow each other intermixed.
     * <p>
     * The segments are linked one after the other and when the macro is evaluated all segments are processed one after
     * the other only one. This ensures that a string, that is the same as the name of a loop variable is not replaced
     * by the actual value of the variable in case the string was part of the value of another, or the same variable.
     *
     * @param variables we want to replace
     * @param content   the content of the macro to be split up into segments
     * @return the first {@link Segment}
     */
    private static Segment splitContentToSegments(String[] variables, String content) {
        final var root = new Segment(null, content);
        for (final var variable : variables) {
            var it = root;
            while (it != null) {
                final var next = it.nextSeg;
                it.split(variable);
                it = next;
            }
        }
        return root;
    }

    private static void checkEqualSign(Input input) throws BadSyntaxAt {
        if (firstCharIs(input, '=')) {
            skip(input, 1);
        } else {
            throw new BadSyntaxAt("for macro has bad syntax, missing '=' at '" + input + "'", input.getPosition());
        }
    }

    private String getValuesStringFromStringTerminatedList(Input input) throws BadSyntaxAt {
        final String valuesString;
        skip(input, 1);
        int closingTick = input.indexOf("`");
        BadSyntaxAt.when(closingTick == -1,"There is no closing '`' before the values in the for macro.",input.getPosition());
        final var stopString = "`" + input.substring(0, closingTick + 1);
        skip(input, closingTick + 1);
        int closing = input.indexOf(stopString);
        BadSyntaxAt.when(closing == -1,"There is no closing " + stopString + " for the values in the for macro.",input.getPosition());
        valuesString = input.substring(0, closing);
        skip(input, closing + stopString.length());
        return valuesString;
    }

    private static String getValuesStringFromSimpleList(Input input) throws BadSyntaxAt {
        final String valuesString;
        skip(input, 1);
        int closing = input.indexOf(")");
        BadSyntaxAt.when(closing == -1,"There is no closing ')' for the values in the for macro.",input.getPosition());
        valuesString = input.substring(0, closing);
        skip(input, closing + 1);
        return valuesString;
    }

    private static KeyWord checkKeyword(Input input) throws BadSyntaxAt {
        final var keyword = fetchId(input);
        skipWhiteSpaces(input);
        switch (keyword) {
            case "in":
                return KeyWord.IN;
            case "from":
                return KeyWord.FROM;
            default:
                throw new BadSyntaxAt("The keyword 'in/from' is missing in the 'for' macro '" + input + "'", input.getPosition());
        }
    }

    private static String[] getVariables(Input input) throws BadSyntaxAt {
        final String[] variables;
        if (firstCharIs(input, '(')) {
            variables = getParameters(input, "for loop");
        } else {
            variables = new String[]{fetchId(input)};
        }
        return variables;
    }

    /**
     * A segment is a String and a link to the next segment. That way the segment is a linked list of strings. The
     * reason to code it this way instead of a standard linked list is that we can easily split segments along the
     * variable names.
     */
    private static class Segment {
        Segment nextSeg;
        String text;
        final boolean isText;

        Segment(Segment nextSeg, String text) {
            this(nextSeg, text, true);
        }

        Segment(Segment nextSeg, String text, boolean isText) {
            this.nextSeg = nextSeg;
            this.text = text;
            this.isText = isText;
        }

        private static void split(final Segment root, final String parameter) {
            var it = root;
            //noinspection StatementWithEmptyBody
            while ((it = splitAndGetNext(it, parameter)) != null) ;
        }

        /**
         * If the segment contains the string {@code parameter} then split it into three parts and return the third
         * one.
         * <p>
         * For example, the parameter is {@code AAA} then
         *
         * <pre>{@code
         *  xxxxxxAAAzzzzzzzz
         * }</pre>
         * <p>
         * will create three segments. The original segment will be modified to
         *
         * <pre>{@code
         *  xxxxxx
         * }</pre>
         * <p>
         * The next segment will be
         *
         * <pre>{@code
         *  AAA
         * }</pre>
         * <p>
         * the last segment will be
         *
         * <pre>{@code
         *  zzzzzzzz
         * }</pre>
         * <p>
         * The code will return the last segment or {@code null} if the parameter is not in the segment.
         *
         * @param it        the segment to split
         * @param parameter along which the segment is split
         * @return the next segment after the splitting
         */
        private static Segment splitAndGetNext(final Segment it, final String parameter) {
            if (!it.isText) {
                return null;
            }
            final var start = it.text.indexOf(parameter);
            if (start < 0) {
                return null;
            }
            final var textSeg = new Segment(it.nextSeg, it.text.substring(start + parameter.length()));
            it.nextSeg = new Segment(textSeg, parameter, false);
            it.text = it.text.substring(0, start);
            return textSeg;
        }

        String content(Map<String, String> params) {
            return isText ? text : params.get(text);
        }

        Segment next() {
            return nextSeg;
        }

        void split(String parameter) {
            split(this, parameter);
        }
    }
}
