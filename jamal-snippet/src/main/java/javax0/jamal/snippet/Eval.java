package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.OptionsControlled;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

import static javax0.jamal.tools.Input.makeInput;

public class Eval implements Macro, OptionsControlled, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var file = scanner.str(null, "file").defaultValue(null);
        final var line = scanner.number(null, "line").defaultValue(1);
        final var snippet = scanner.str(null, "snippet").defaultValue(null);
        scanner.done();
        BadSyntax.when((line.isPresent() || file.isPresent()) && snippet.isPresent(), "Either 'line' and/or 'file' or 'snippet' must be present as option on '\"+getId()+\"' macro.\"");
        BadSyntax.when(!file.isPresent() && !snippet.isPresent(),"Either 'file' or 'snippet' must be present as option on '"+getId()+"' macro.");
        InputHandler.skipWhiteSpaces(in);
        BadSyntax.when(in.isEmpty() && !snippet.isPresent(), "Missing snippet");
        final Position pos;
        if (snippet.isPresent()) {
            pos = SnippetStore.getInstance(processor).fetchSnippet(snippet.get()).pos;
        } else {
            final var fn = FileTools.absolute(in.getReference(), file.get());
            pos = new Position(fn, line.get(), 1);
        }
        final Input input;
        if (!in.isEmpty()) {
            input = makeInput(in.toString(), pos);
        } else {
            final var snip = SnippetStore.getInstance(processor).fetchSnippet(snippet.get());
            input = makeInput(snip.text, snip.pos);
        }

        return processor.process(input);
    }

    @Override
    public String getId() {
        return "snip:eval";
    }
}
