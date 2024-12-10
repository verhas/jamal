package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;

public class Reflow implements Macro, InnerScopeDependent, BlockConverter, Scanner.FirstLine {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var width = scanner.number("width").defaultValue(0);
        scanner.done();
        final var sb = new StringBuilder(in);
        convertTextBlock(processor, sb, in.getPosition(), width.getParam());
        in.replace(sb);
        return in.toString();
    }

    @Override
    public void convertTextBlock(Processor processor, final StringBuilder sb, final Position pos, final Params.Param<?>... params) throws BadSyntax {
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
