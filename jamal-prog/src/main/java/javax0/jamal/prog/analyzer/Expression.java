package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.prog.commands.Operation;

/**
 * An expression is
 * <p>
 * <pre>
 * 'not' ( expression )
 * expression1 'and' expression
 * expression1 'or' expression
 * expression1
 * </pre>
 */

public class Expression {
    public static javax0.jamal.prog.commands.Expression analyze(final Lex.List lexes) throws BadSyntax {
        if (!lexes.hasNext()) {
            throw new BadSyntax("Expression is empty");
        }
        var left = Expression1.analyze(lexes);
        while (lexes.is("or")) {
            final var op = lexes.next().text;
            final var right = Expression1.analyze(lexes);
            left = new Operation(op, left, right);
        }
        return left;
    }

    static javax0.jamal.prog.commands.Expression getExpressionBetweenParenthese(final Lex.List lexes) throws BadSyntax {
        lexes.next();
        final var expression = Expression.analyze(lexes);
        if (lexes.is(")")) {
            lexes.next();
            return expression;
        }
        throw new BadSyntax("Expression is not well formed, missing ')'");
    }
}
