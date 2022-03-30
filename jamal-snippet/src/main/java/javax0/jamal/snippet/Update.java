package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class Update implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var head = Params.<String>holder("head").orElse("");
        final var tail = Params.<String>holder("tail").orElse("");

        final var start = Params.<Pattern>holder("start").orElse(
            "^\\s*" +
                Pattern.quote(processor.getRegister().open()) +
                "\\s*(?:#|@)\\s*snip\\s+([$_:a-zA-Z][$_:a-zA-Z0-9]*)\\s*$").asPattern();
        final var stop = Params.<Pattern>holder("stop").orElse(
            "^\\s*" + Pattern.quote(processor.getRegister().close()) + "\\\\?\\s*$").asPattern();
        Scan.using(processor).from(this).firstLine().keys(head, tail, start, stop).parse(in);

        final var snippets = SnippetStore.getInstance(processor);
        final var state = new State(snippets, processor, head.get(), tail.get(), start.get(), stop.get());
        final var sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(in.getPosition().file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(replace(state, line));
            }
            if (state.skipping) {
                throw new BadSyntaxAt("The snip macro is not terminated for 'update'.", new Position(in.getPosition().file, state.lastOpen, 1));
            }
            try (final var output = new FileOutputStream(in.getPosition().file)) {
                output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new BadSyntaxAt("File " + in.getPosition().file + " cannot be read.", in.getPosition());
        }
        return "";
    }

    private static class State {
        boolean skipping = false;
        int lineNr = 0;
        int lastOpen = 0;
        final SnippetStore snippets;
        final String head;
        final String tail;
        final Pattern start;
        final Pattern stop;

        private State(SnippetStore snippets, Processor processor, String head, String tail, Pattern start, Pattern stop) {
            this.snippets = snippets;
            this.head = head;
            this.tail = tail;
            this.start = start;
            this.stop = stop;
        }
    }

    private static String replace(State state, String line) throws BadSyntax {
        state.lineNr++;
        if (state.skipping) {
            if (state.stop.matcher(line).matches()) {
                state.skipping = false;
                return line + "\n";
            }
            return "";
        } else {
            final var matcher = state.start.matcher(line);
            if (matcher.matches()) {
                state.skipping = true;
                state.lastOpen = state.lineNr;
                var snipText = state.snippets.snippet(matcher.group(1));
                if (!snipText.endsWith("\n")) {
                    snipText += "\n";
                }
                return line + "\n" + state.head + snipText + state.tail;
            }
            return line + "\n";
        }
    }

    @Override
    public String getId() {
        return "snip:update";
    }
}
