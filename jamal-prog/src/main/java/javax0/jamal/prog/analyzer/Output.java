package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;

public class Output {


    public static javax0.jamal.prog.commands.Output analyze(final Lex.List lexes) throws BadSyntax {
        lexes.next();
        final var value = Expression.analyze(lexes);
        lexes.eol("Expected new line after << expression");
        return new javax0.jamal.prog.commands.Output(value);
    }
}
