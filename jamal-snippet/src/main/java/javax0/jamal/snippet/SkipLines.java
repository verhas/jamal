package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SkipLines implements Macro, InnerScopeDependent, BlockConverter {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var skipStart = Params.<Pattern>holder("skip").orElse("skip").asPattern();
        final var skipEnd = Params.<Pattern>holder("endSkip").orElse("end\\s+skip").asPattern();
        Scan.using(processor).from(this).firstLine().keys(skipEnd, skipStart).parse(in);

        convertTextBlock(in.getSB(), in.getPosition(), skipStart, skipEnd);
        return in.toString();
    }

    @Override
    public void convertTextBlock(final StringBuilder sb, final Position pos, final Params.Param<?>... params) throws BadSyntax {
        assertParams(2, params);
        final var skipStart = params[0].asType(Pattern.class);
        final var skipEnd = params[1].asType(Pattern.class);

        final var lines = sb.toString().split("\n", -1);
        int from = 0;
        int to = 0;
        boolean skipping = false;
        boolean lastLineCopied = false;
        while (from < lines.length) {
            if (skipping) {
                if (skipEnd.get().matcher(lines[from]).find()) {
                    skipping = false;
                }
            } else {
                if (skipStart.get().matcher(lines[from]).find()) {
                    skipping = true;
                } else {
                    lines[to] = lines[from];
                    lastLineCopied = from == lines.length - 1;
                    to++;
                }
            }
            from++;
        }
        joinLines(sb, lines, to, lastLineCopied);
    }

    static void joinLines(final StringBuilder sb, final String[] lines, final int to, final boolean lastLineCopied) {
        final var joined = Arrays.stream(lines).limit(to).collect(Collectors.joining("\n"));
        final var extraNl = !needsNoExtraNl(sb.toString(), lastLineCopied, joined);
        sb.setLength(0);
        sb.append(joined);
        if (extraNl) {
            sb.append("\n");
        }
    }

    private static boolean needsNoExtraNl(String in, boolean lastLineCopied, String joined) {
        return joined.length() == 0 || joined.charAt(joined.length() - 1) == '\n' || (lastLineCopied && in.charAt(in.length() - 1) != '\n');
    }

    @Override
    public String getId() {
        return "skipLines";
    }
}
