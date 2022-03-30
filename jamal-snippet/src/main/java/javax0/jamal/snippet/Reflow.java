package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

public class Reflow implements Macro, InnerScopeDependent, BlockConverter {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var width = Params.holder("width").orElseInt(0);
        Scan.using(processor).from(this).firstLine().keys(width).parse(in);
        convertTextBlock(in.getSB(), in.getPosition(), width);
        return in.toString();
    }

    @Override
    public void convertTextBlock(final StringBuilder sb, final Position pos, final Params.Param<?>... params) throws BadSyntax {
        assertParams(1, params);
        final var width = params[0].asInt();
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
    }
}
