package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.engine.macro.Segment;
import javax0.jamal.engine.macro.TextSegment;

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

    private static final Pattern pattern = Pattern.compile("\\s+(\\w[\\w\\d_$]*)\\s+in\\s*\\((.*?)\\)\\s*=(.*)");

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        var input = in.getInput();
        var matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            throw new BadSyntax("use macro has bad syntax '" + input + "'");
        }
        final var loopVariable = matcher.group(1);
        final var values = matcher.group(2);
        final var content = matcher.group(3);
        final var opt = processor.getRegister().getUserMacro("$forsep");
        final var splitter = opt.isPresent() ? opt.get().evaluate() : ",";
        final var valueList = values.split(splitter);
        final var output = new StringBuilder();
        final var root = new TextSegment(null, content);
        root.split(loopVariable);
        for (final String value : valueList) {
            for (Segment segment = root; segment != null; segment = segment.next()) {
                if (segment instanceof TextSegment) {
                    output.append(segment.content());
                } else {
                    output.append(value);
                }
            }
        }
        return output.toString();
    }
}
