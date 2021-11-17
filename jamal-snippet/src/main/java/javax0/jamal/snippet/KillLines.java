package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax0.jamal.snippet.SkipLines.needsNoExtraNl;

public class KillLines implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var pattern = Params.holder("pattern").orElse("^\\s*$").as(Pattern.class, Pattern::compile);
        final var keep = Params.<Boolean>holder("keep").asBoolean();
        Params.using(processor).from(this).keys(pattern,keep).parse(in);

        final var lines = in.toString().split("\n", -1);
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
        final var joined = Arrays.stream(lines).limit(to).collect(Collectors.joining("\n"));
        if (needsNoExtraNl(in, lastLineCopied, joined)) {
            return joined;
        } else {
            return joined + "\n";
        }
    }

    @Override
    public String getId() {
        return "killLines";
    }
}
