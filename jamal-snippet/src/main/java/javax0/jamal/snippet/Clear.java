package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class Clear implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        SnippetStore.getInstance(processor).clear();
        return "";
    }
}
