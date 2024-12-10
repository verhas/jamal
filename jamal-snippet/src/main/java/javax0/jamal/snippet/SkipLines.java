package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SkipLines implements Macro, InnerScopeDependent, BlockConverter, Scanner.FirstLine {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in,processor);
        final var skipStart = scanner.pattern("skip").defaultValue("skip");
        final var skipEnd = scanner.pattern("endSkip").defaultValue("end\\s+skip");
        scanner.done();

        final var sb = new StringBuilder(in);
        convertTextBlock(processor, sb, in.getPosition(), skipStart.getParam(), skipEnd.getParam());
        in.replace(sb);
        return in.toString();
    }

    @Override
    public void convertTextBlock(Processor processor, final StringBuilder sb, final Position pos, final Params.Param<?>... params) throws BadSyntax {
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
        return joined.isEmpty() || joined.charAt(joined.length() - 1) == '\n' || (lastLineCopied && in.charAt(in.length() - 1) != '\n');
    }

    @Override
    public String getId() {
        return "skipLines";
    }
}
