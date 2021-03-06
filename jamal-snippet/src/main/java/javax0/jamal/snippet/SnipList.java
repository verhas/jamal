package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.util.stream.Collectors;

import static javax0.jamal.tools.Params.holder;

public class SnipList implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var idRegex = holder("name", "id").orElse("").asString();
        final var fnRegex = holder("file").orElse("").asString();
        final var textRegex = holder("text").orElse("").asString();
        final var listSeparator = holder("listSeparator").orElse(",").asString();
        Params.using(processor).from(this).keys(idRegex,fnRegex,textRegex,listSeparator).tillEnd().parse(in);
        final var store = SnippetStore.getInstance(processor);
        return String.join(listSeparator.get(),
            store.snippetList(idRegex.get(), fnRegex.get(), textRegex.get())
                .map(s -> s.id)
                .collect(Collectors.toList()));
    }

    @Override
    public String getId() {
        return "snip:list";
    }
}
