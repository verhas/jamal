package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scan;

import java.util.stream.Collectors;

import static javax0.jamal.tools.Params.holder;

public class SnipList implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var idRegex = holder("name", "id").orElse("").asString();
        final var fnRegex = holder("file", "fileName").orElse("").asString();
        final var textRegex = holder("text", "contains").orElse("").asString();
        final var listSeparator = holder("listSeparator").orElse(",").asString();
        Scan.using(processor).from(this).tillEnd().keys(idRegex, fnRegex, textRegex, listSeparator).parse(in);
        final var store = SnippetStore.getInstance(processor);
        return store.snippetList(idRegex.get(), fnRegex.get(), textRegex.get())
            .map(s -> s.id)
            .collect(Collectors.joining(listSeparator.get()));
    }

    @Override
    public String getId() {
        return "snip:list";
    }
}
