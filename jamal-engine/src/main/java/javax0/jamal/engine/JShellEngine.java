package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class JShellEngine implements javax0.jamal.api.JShellEngine {

    private final JShell js;
    private final ByteArrayOutputStream output;

    public JShellEngine() {
        output = new ByteArrayOutputStream();
        js = JShell.builder().out(new PrintStream(output)).build();
    }

    public String evaluate(String input) throws BadSyntax {
        output.reset();
        final var events = js.eval(input);
        for (SnippetEvent e : events) {
            if (e.status() == Snippet.Status.RECOVERABLE_NOT_DEFINED || e.status() == Snippet.Status.REJECTED || e.exception() != null) {
                throw new BadSyntax("The jshell snippet '" + e.snippet().source() + "' produced error.", e.exception());
            }
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    public void define(String input) throws BadSyntax {
        final var events = js.eval(input);
        for (SnippetEvent e : events) {
            if (e.status() == Snippet.Status.REJECTED || e.exception() != null) {
                throw new BadSyntax("The jshell snippet '" + e.snippet().source() + "' produced error.", e.exception());
            }
        }
    }

    @Override
    public void close() {
        js.close();
    }
}
