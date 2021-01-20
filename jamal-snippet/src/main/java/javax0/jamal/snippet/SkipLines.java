package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.MacroReader;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces2EOL;

public class SkipLines implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var reader = MacroReader.macro(processor);
        final var skipStart = Pattern.compile(reader.readValue("skip").orElse("skip"));
        final var skipEnd = Pattern.compile(reader.readValue("endSkip").orElse("end\\s+skip"));
        skipWhiteSpaces2EOL(in);
        final var lines = in.toString().split("\n", -1);
        int from = 0;
        int to = 0;
        boolean skipping = false;
        boolean lastLineCopied = false;
        while (from < lines.length) {
            if (skipping) {
                if (skipEnd.matcher(lines[from]).find()) {
                    skipping = false;
                }
            } else {
                if (skipStart.matcher(lines[from]).find()) {
                    skipping = true;
                } else {
                    lines[to] = lines[from];
                    lastLineCopied = from == lines.length - 1;
                    to++;
                }
            }
            from++;
        }
        final var joined = Arrays.stream(lines).limit(to).collect(Collectors.joining("\n"));
        if (needsNoExtraNl(in, lastLineCopied, joined)) {
            return joined;
        } else {
            return joined + "\n";
        }
    }

    static boolean needsNoExtraNl(Input in, boolean lastLineCopied, String joined) {
        return joined.length() == 0 || joined.charAt(joined.length() - 1) == '\n' || (lastLineCopied && in.toString().charAt(in.length() - 1) != '\n');
    }

    @Override
    public String getId() {
        return "skipLines";
    }
}
