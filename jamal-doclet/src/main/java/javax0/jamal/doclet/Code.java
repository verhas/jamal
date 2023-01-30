package javax0.jamal.doclet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

/**
 * This macro helps to overcome the fact that JavaDoc {@code code} tag look like a macro.
 * When used in a segment where the macro processor is active it will be processed as a macro.
 * The outcome of the macro processing will be similar to what it would be out of the macro processing scope.
 */
public class Code implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        return "<code>" + escapeHTML(processor.process(in)) + "</code>";
    }

    private static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
