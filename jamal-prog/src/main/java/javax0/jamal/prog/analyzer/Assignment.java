package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;

public class Assignment {

    public static javax0.jamal.prog.commands.Assignment analyze(final Lex.List lexes) throws BadSyntax {
        final var variable = lexes.next().text;
        lexes.assume(Lex.Type.RESERVED, "=", "Expected '=' after for variable");
        final var expression = Expression.analyze(lexes);
        lexes.eol("Expected new line after assignment");
        return new javax0.jamal.prog.commands.Assignment(variable, expression);
    }
}
