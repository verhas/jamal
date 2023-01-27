package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.prog.commands.Operation;

import static javax0.jamal.prog.analyzer.Expression.getExpressionBetweenParenthese;

/**
 * An expression1 is
 * <p>

 * expression2 '==' expression1
 * expression2 '!=' expression1
 * expression2 '<' expression1
 * expression2 '<=' expression1
 * expression2 '>' expression1
 * expression2 '>=' expression1
 * expression2
 */

public class Expression1 {
    public static javax0.jamal.prog.commands.Expression analyze(final Lex.List lexes) throws BadSyntax {
        if (lexes.isEmpty()) {
            throw new BadSyntax("Expression is empty");
        }
        var expression3 = Expression2.analyze(lexes);
        if (lexes.is("==") || lexes.is("!=") || lexes.is("<") || lexes.is("<=") || lexes.is(">") || lexes.is(">=")) {
            final var op = lexes.next().text;
            return new Operation(op, expression3, Expression1.analyze(lexes));
        }
        return expression3;
    }


}
