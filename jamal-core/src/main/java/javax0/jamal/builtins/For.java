package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Define the {@code for} looping macro. The syntax of the macro is
 *
 * <pre>
 *     {#for var in (a,b,c,d)= var is either a, b, c or d
 *     }
 * </pre>
 * <p>
 * The default separator is {@code ,} (comma), but it can be redefined assigning a value to the user defined
 * macro {@code $forsep}.
 */
public class For implements Macro {

    private static final Pattern pattern = Pattern.compile("\\s+(\\w[\\w\\d_$]*)\\s+in\\s*\\((.*?)\\)\\s*=(.*)", Pattern.DOTALL);

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        var matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            throw new BadSyntax("use macro has bad syntax '" + input + "'");
        }
        final var loopVariable = matcher.group(1);
        final var values = matcher.group(2);
        final var content = matcher.group(3);
        final var optionalForSepMacro = processor.getRegister().getUserMacro("$forsep");
        final var splitter = optionalForSepMacro.isPresent() ? optionalForSepMacro.get().evaluate() : ",";
        final var valueList = values.split(splitter);
        final var output = new StringBuilder();
        final var root = new Segment(null, content);
        root.split(loopVariable);
        for (final String value : valueList) {
            for (Segment segment = root; segment != null; segment = segment.next()) {
                output.append(segment.content().orElse(value));
            }
        }
        return output.toString();
    }

    private static class Segment {
        Segment nextSeg;
        String text;

        Segment(Segment nextSeg, String text) {
            this.nextSeg = nextSeg;
            this.text = text;
        }

        private static void split(final Segment root, final String parameter) {
            var it = root;
            while ((it = splitAndGetNext(it, parameter)) != null) ;
        }

        private static Segment splitAndGetNext(final Segment it, final String parameter) {
            final var start = it.text.indexOf(parameter);
            if (start < 0) {
                return null;
            }
            final var textSeg = new Segment(it.nextSeg, it.text.substring(start + parameter.length()));
            it.nextSeg = new Segment(textSeg, null);
            it.text = it.text.substring(0, start);
            return textSeg;
        }

        Optional<String> content() {
            return Optional.ofNullable(text);
        }

        Segment next() {
            return nextSeg;
        }

        void split(String parameter) {
            split(this, parameter);
        }
    }
}
