package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.OptionsStore;
import javax0.jamal.tools.Params;

import static javax0.jamal.tools.Params.holder;

public class Replace implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var isRegex = holder("regex").asBoolean();
        Params.using(processor).from(this).keys(isRegex).startWith('(').endWith(')').parse(in);
        InputHandler.skipWhiteSpaces(in);
        final var parts = InputHandler.getParts(in);
        if (parts.length < 2) {
            throw new BadSyntax("Marco 'replace' needs at least two arguments, gon only "
                + parts.length
                + ":\n" + String.join("\n", parts)
                + "\n----------");
        }
        String string = parts[0];
        for (int i = 1; i < parts.length; i += 2) {
            final var from = parts[i];
            final String to;
            if (i < parts.length - 1) {
                to = parts[i + 1];
            } else {
                to = "";
            }
            if (isRegex.get()) {
                try {
                    string = string.replaceAll(from, to);
                } catch (IllegalArgumentException e) {
                    throw new BadSyntax("There is a problem with the regular expression in macro 'replace' : "
                        + from + "\n" + to + "\n", e);
                }
            } else {
                string = string.replace(from, to);
            }
        }
        return string;
    }
}
