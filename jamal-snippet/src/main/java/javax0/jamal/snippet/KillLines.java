package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;

import java.util.regex.Pattern;

public class KillLines implements Macro, InnerScopeDependent, BlockConverter, Scanner.FirstLine {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var pattern = scanner.pattern("kill", "pattern").defaultValue("^\\s*$");
        final var keep = scanner.bool("keep");
        scanner.done();

        convertTextBlock(processor, in.getSB(), in.getPosition(), pattern.getParam(), keep.getParam());
        return in.toString();
    }

    private static final String[] IDS = new String[]{"killLines", "filterLines"};
    @Override
    public String[] getIds() {
        return IDS;
    }

    @Override
    public void convertTextBlock(Processor processor, final StringBuilder sb, final Position pos, final Params.Param<?>... params) throws BadSyntax {
        assertParams(2, params);
        final var pattern = params[0].asType(Pattern.class);
        final var keep = params[1].asBoolean();
        final var lines = sb.toString().split("\n", -1);
        int from = 0, to = 0;
        boolean lastLineCopied = false;
        while (from < lines.length) {
            if (keep.is() == pattern.get().matcher(lines[from]).find()) {
                lines[to] = lines[from];
                lastLineCopied = from == lines.length - 1;
                to++;
            }
            from++;
        }
        SkipLines.joinLines(sb, lines, to, lastLineCopied);
    }

}
