package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;

public class Reflow implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var width = Params.holder("width").orElseInt(0);
        Params.using(processor).from(this).keys(width).parse(in);
        final var sb = in.getSB();
        int index = 0;
        int relative = 0;
        int lastSpace = -1;
        while (index < sb.length()) {
            if (sb.charAt(index) == '\n' && index < sb.length() - 2 && sb.charAt(index + 1) == '\n' && sb.charAt(index + 2) == '\n') {
                sb.delete(index, index + 1);
                continue;
            }
            if (sb.charAt(index) == '\n' && index < sb.length() - 1 && sb.charAt(index + 1) == '\n') {
                index += 2;
                relative = 0;
                continue;
            }
            if (sb.charAt(index) == '\n') {
                sb.replace(index, index + 1, " ");
            }
            if (Character.isWhitespace(sb.charAt(index))) {
                lastSpace = index;
            }
            if (width.get() > 0 && relative >= width.get() && lastSpace >= 0) {
                sb.replace(lastSpace, lastSpace + 1, "\n");
                lastSpace = -1;
                relative = 0;
                index++;
            } else {
                index++;
                relative++;
            }

        }
        return sb.toString();
    }
}
