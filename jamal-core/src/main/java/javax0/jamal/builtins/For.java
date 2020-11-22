package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.getParameters;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Define the {@code for} looping macro. The syntax of the macro is
 *
 * <pre>
 *     {#for var in (a,b,c,d)= var is either a, b, c or d
 *     }
 * </pre>
 * <p>
 * The default separator is {@code ,} (comma), but it can be redefined
 * to be any regular expression assigning a value to the user defined
 * macro {@code $forsep}.
 */
public class For implements Macro {

    private static final Pattern PATTERN = Pattern.compile("in\\s*\\((.*?)\\)\\s*=(.*)", Pattern.DOTALL);

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        skipWhiteSpaces(input);

        final String[] variables;
        if (firstCharIs(input, '(')) {
            variables = getParameters(input, "for loop");
        } else {
            variables = new String[]{fetchId(input)};
        }
        skipWhiteSpaces(input);
        var matcher = PATTERN.matcher(input);
        if (matcher.matches()) {
            final var valuesString = matcher.group(1);
            final var content = matcher.group(2);
            final String splitter = getSplitter(processor, "$forsep", ",");
            final String subsplitter = getSplitter(processor, "$forsubsep", "\\|");
            final var valueList = valuesString.split(splitter);
            final var output = new StringBuilder();
            final var root = new Segment(null, content);
            for (final var variable : variables) {
                var it = root;
                while (it != null) {
                    final var next = it.nextSeg;
                    it.split(variable);
                    it = next;
                }
            }
            final var parameterMap = new HashMap<String, String>();
            for (final String value : valueList) {
                final var values = value.split(subsplitter);
                if (values.length != variables.length) {
                    throw new BadSyntax("number of the values does not match the number of the parameters\n" +
                        String.join(",", variables) + "\n" + value);
                }
                for (int i = 0; i < variables.length; i++) {
                    parameterMap.put(variables[i], values[i]);
                }
                for (Segment segment = root; segment != null; segment = segment.next()) {
                    output.append(segment.content(parameterMap));
                }
            }
            return output.toString();
        } else {
            throw new BadSyntax("use macro has bad syntax '" + input + "'");
        }
    }

    private String getSplitter(Processor processor, String macro, String defautl) throws BadSyntax {
        final var optionalForSepMacro = processor.getRegister().getUserDefined(macro);
        final var splitter = optionalForSepMacro
            .filter(ud -> ud instanceof Evaluable)
            .map(ud -> (Evaluable) ud)
            .map(udm -> {
                try {
                    return udm.evaluate();
                } catch (BadSyntax bs) {
                    return null;
                }
            })
            .orElse(defautl);

        if (splitter == null) {
            throw new BadSyntax("Macro $sep cannot be evaluated");
        }
        return splitter;
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
         * @param it
         * @param parameter
         * @return
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
