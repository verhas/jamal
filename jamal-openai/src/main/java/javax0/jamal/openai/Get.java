package javax0.jamal.openai;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class Get implements Macro , InnerScopeDependent {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var opt = new Options(processor, in, this);

        try {
            return new Query(opt.cacheSeed).get(opt.url);
        } catch (final Exception e) {
            throw new BadSyntax("GET url '" + opt.url + "' failed", e);
        }
    }

    @Override
    public String getId() {
        return "openai:get";
    }
}
