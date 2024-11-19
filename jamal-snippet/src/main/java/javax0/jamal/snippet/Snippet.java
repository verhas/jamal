package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Scanner;

import static javax0.jamal.tools.InputHandler.*;

public class Snippet implements Macro, OptionsControlled, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var snippet = scanner.str(null, "ref").optional();
        final var file = scanner.str(null, "file").optional();
        final var line = scanner.number(null, "line").defaultValue(1);
        scanner.done();
        BadSyntax.when((line.isPresent() || file.isPresent()) && snippet.isPresent(), "Either 'line' or 'file' or 'snippet' must be present");
        skipWhiteSpaces(in);
        final var id = fetchId(in);
        skipWhiteSpaces(in);
        BadSyntax.when(!firstCharIs(in, '='), "snippet '%s' has no '=' to body", id);
        skip(in, 1);
        skipWhiteSpaces2EOL(in);
        final Position pos;
        if (snippet.isPresent()) {
            pos = SnippetStore.getInstance(processor).fetchSnippet(snippet.get()).pos;
        } else if (file.isPresent()) {
            final var fn = FileTools.absolute(in.getReference(), file.get());
            pos = new Position(fn, line.get(), 1);
        } else {
            pos = in.getPosition();
        }
        SnippetStore.getInstance(processor).snippet(id, in.toString(), pos);
        return "";
    }

    @Override
    public String getId() {
        return "snip:define";
    }
}
