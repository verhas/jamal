package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.prog.commands.Operation;

/**
 * An expression is
 * <p>
 * 'not' ( expression )
 * expression1 'and' expression
 * expression1 'or' expression
 * expression1
 */

public class Expression {
    public static javax0.jamal.prog.commands.Expression analyze(final Lex.List lexes) throws BadSyntax {
        if (!lexes.hasNext()) {
            throw new BadSyntax("Expression is empty");
        }
        if (lexes.is("@")) {
            lexes.next();
            return new Operation("@", null, Expression.analyze(lexes));
        }
        if (lexes.is("not")) {
            lexes.next();
            lexes.assume(Lex.Type.RESERVED, "(");
            final var op = new Operation("not", null, Expression.analyze(lexes));
            lexes.assume(Lex.Type.RESERVED, ")");
            return op;
        }
        var expression1 = Expression1.analyze(lexes);
        if (lexes.is("and") || lexes.is("or")) {
            final var op = lexes.next().text;
            return new Operation(op, expression1, Expression.analyze(lexes));
        }
        return expression1;
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
