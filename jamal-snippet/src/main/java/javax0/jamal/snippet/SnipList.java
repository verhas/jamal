package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;

import java.util.stream.Collectors;

public class SnipList implements Macro, InnerScopeDependent, Scanner.WholeInput {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var idRegex = scanner.str("name", "id").defaultValue("");
        final var fnRegex = scanner.str("file", "fileName").defaultValue("");
        final var textRegex = scanner.str("text", "contains").defaultValue("");
        final var listSeparator = scanner.str("listSeparator").defaultValue(",");
        scanner.done();
        final var store = SnippetStore.getInstance(processor);
        return store.snippetList(idRegex.get(), fnRegex.get(), textRegex.get())
                .filter(snip -> snip.exception == null)
                .map(s -> s.id)
                .collect(Collectors.joining(listSeparator.get()));
    }

    @Override
    public String getId() {
        return "snip:list";
    }
}
