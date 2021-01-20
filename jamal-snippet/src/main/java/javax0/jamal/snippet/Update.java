package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.MacroReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class Update implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var snippets = SnippetStore.getInstance(processor);
        final var state = new State(snippets, processor);
        final var sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(in.getPosition().file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(replace(state,line));
            }
            try( final var output = new FileOutputStream(new File(in.getPosition().file))){
                output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new BadSyntaxAt("File " + in.getPosition().file + " cannot be read.", in.getPosition());
        }
        System.out.println(sb.toString());
        return "";
    }

    private static class State {
        boolean skipping = false;
        final SnippetStore snippets;
        final String head;
        final String tail;
        final Pattern start;
        final Pattern stop;

        private State(SnippetStore snippets, Processor processor) throws BadSyntax {
            this.snippets = snippets;
            final var reader = MacroReader.macro(processor);
            head = reader.readValue("head").orElse("");
            tail = reader.readValue("tail").orElse("");
            start = Pattern.compile(reader.readValue("start").orElse(
                "^\\s*" +
                    Pattern.quote(processor.getRegister().open()) +
                    "\\s*(?:#|@)\\s*snip\\s+([$_:a-zA-Z][$_:a-zA-Z0-9]*)\\s*.*$"));
            stop = Pattern.compile(reader.readValue("start").orElse(
                "^\\s*" + Pattern.quote(processor.getRegister().close()) + "\\s*$"));
        }
    }

    private String replace(State state, String line) throws BadSyntax {
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
                return line + "\n" + state.head + state.snippets.snippet(matcher.group(1)) + state.tail;
            }
            return line + "\n";
        }
    }

    @Override
    public String getId() {
        return "snip:update";
    }
}
