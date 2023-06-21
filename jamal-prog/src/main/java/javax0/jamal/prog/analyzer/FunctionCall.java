package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.prog.commands.Expression;

import java.util.ArrayList;

public class FunctionCall {
    public static Expression analyze(Lex lex, Lex.List lexes) throws BadSyntax {
        final var function = lex.text;
        lexes.next(); // consume the '('
        final var arguments = new ArrayList<Expression>();
        while (!lexes.is(")")) {
            arguments.add(javax0.jamal.prog.analyzer.Expression.analyze(lexes));
            if (lexes.is(",")) {
                lexes.next();
            }
        }
        BadSyntax.when(!lexes.is(")"), "Function call: expected ')' after the arguments");
        lexes.next(); // consume the ')'
        return new javax0.jamal.prog.commands.FunctionCall(function, arguments.toArray(Expression[]::new));
    }
}
