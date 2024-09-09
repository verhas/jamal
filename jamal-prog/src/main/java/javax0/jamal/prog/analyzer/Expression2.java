package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.prog.commands.Operation;

/**
 * An expression1 ::=
 * <pre>
 * expression2 '==' expression1
 * expression2 '!=' expression1
 * expression2 '<' expression1
 * expression2 '<=' expression1
 * expression2 '>' expression1
 * expression2 '>=' expression1
 * expression2
 * </pre>
 */

public class Expression2 {
    public static javax0.jamal.prog.commands.Expression analyze(final Lex.List lexes) throws BadSyntax {
        if (lexes.isEmpty()) {
            throw new BadSyntax("Expression is empty");
        }
        if (lexes.is("not")) {
            lexes.next();
            return new Operation("not", null, Expression2.analyze(lexes));
        }
        var left = Expression3.analyze(lexes);
        while (lexes.is("==") || lexes.is("!=") || lexes.is("<") || lexes.is("<=") || lexes.is(">") || lexes.is(">=")) {
            final var op = lexes.next().text;
            final var right = Expression3.analyze(lexes);
            left = new Operation(op, left, right);
        }
        return left;
    }


}
