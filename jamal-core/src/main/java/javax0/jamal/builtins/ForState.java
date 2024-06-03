package javax0.jamal.builtins;

import javax0.jamal.api.*;
import javax0.jamal.tools.MacroReader;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.BooleanParameter;
import javax0.jamal.tools.param.StringParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax0.jamal.tools.InputHandler.*;

class ForState {
    StringParameter separator;
    StringParameter subSeparator;
    StringParameter join;
    BooleanParameter trim;
    BooleanParameter skipEmpty;
    BooleanParameter lenient;
    BooleanParameter evalValueList;
    Processor processor;

    public ForState(Scanner.ScannerObject scanner, Processor processor) {
        this.processor = processor;
        // snippet parops_for
        separator = scanner.str("$forsep", "separator", "sep").defaultValue(",");
        // can define the separator if it is different from the default, which is `,` comma.
        // The value is used as a regular expression giving very versatile possibilities.
        subSeparator = scanner.str("$forsubsep", "subseparator", "subsep").defaultValue("\\|");
        // can define the subseparator if it is different from the default, which is `|` pipe.
        // It is used when there are multiple variables in the loop.
        // Similarly to the separator, the value is used as a regular expression.
        trim = scanner.bool("trimForValues", "trim");
        // is a boolean paror.
        // If it is present and `true`, then the values are trimmed, the spaces are removed from the beginning and the end.
        skipEmpty = scanner.bool("skipForEmpty", "skipEmpty");
        // is a boolean parameter.
        // If it is present and `true`, then the empty values are skipped.
        lenient = scanner.bool("lenient");
        // is a boolean parameter.
        // If it is present and `true`, then the number of the values in the value list is not checked against the number of the variables.
        evalValueList = scanner.bool("evaluateValueList", "evalist");
        // is a boolean parameter.
        // If it is present and `true`, then the value list is evaluated as a macro before spling it up to values.
        join = scanner.str("$forjoin", "join").defaultValue("");
        // is used to join the values when the values are joined together.
        // The default is the empty string.
        // end snippet
    }


    String[][] getValueMatrix(Object object, int nrOfVariables) {
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

    String[][] getValueMatrix(Input input, String[] variables, Position pos) throws BadSyntax {
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
            final var macro = MacroReader.macro(processor).readValue(valuesString);
            if (macro.isPresent()) {
                valuesString = macro.get();
            } else {
                valuesString = processor.process(javax0.jamal.tools.Input.makeInput(valuesString, pos));
            }
        }
        final String[] valueArray = valuesString.split(separator.get(), -1);

        final String[][] valueMatrix = new String[valueArray.length][];
        for (int j = 0; j < valueArray.length; j++) {
            final var value = valueArray[j];
            if (!value.isEmpty() || !skipEmpty.is()) {
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
     * The segments are linked one after the other, and when the macro is evaluated, all segments are processed one after
     * the other only one. This ensures that a string, that is the same as the name of a loop variable is not replaced
     * by the actual value of the variable in case the string was part of the value of another, or the same variable.
     *
     * @param variables we want to replace
     * @param content   the content of the macro to be split up into segments
     * @return the first {@link Segment}
     */
    static Segment splitContentToSegments(String[] variables, String content) {
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

    static void checkEqualSign(Input input) throws BadSyntaxAt {
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
        BadSyntaxAt.when(closingTick == -1, "There is no closing '`' before the values in the for macro.", input.getPosition());
        final var stopString = "`" + input.substring(0, closingTick + 1);
        skip(input, closingTick + 1);
        int closing = input.indexOf(stopString);
        BadSyntaxAt.when(closing == -1, "There is no closing " + stopString + " for the values in the for macro.", input.getPosition());
        valuesString = input.substring(0, closing);
        skip(input, closing + stopString.length());
        return valuesString;
    }

    private static String getValuesStringFromSimpleList(Input input) throws BadSyntaxAt {
        final String valuesString;
        skip(input, 1);
        int closing = input.indexOf(")");
        BadSyntaxAt.when(closing == -1, "There is no closing ')' for the values in the for macro.", input.getPosition());
        valuesString = input.substring(0, closing);
        skip(input, closing + 1);
        return valuesString;
    }

    static For.KeyWord checkKeyword(Input input) throws BadSyntaxAt {
        final var keyword = fetchId(input);
        skipWhiteSpaces(input);
        switch (keyword) {
            case "in":
                return For.KeyWord.IN;
            case "from":
                return For.KeyWord.FROM;
            default:
                throw new BadSyntaxAt("The keyword 'in/from' is missing in the 'for' macro '" + input + "'", input.getPosition());
        }
    }

    static String[] getVariables(Input input) throws BadSyntaxAt {
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
    static class Segment {
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
        static Segment splitAndGetNext(final Segment it, final String parameter) {
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
