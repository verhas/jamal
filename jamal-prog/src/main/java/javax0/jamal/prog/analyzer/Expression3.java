package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.prog.commands.Operation;

/**
 * An expression2 is
 * <pre>
 * expression3 '+' expression2
 * expression3 '-' expression2
 * expression3
 * </pre>
 */

public class Expression3 {
    public static javax0.jamal.prog.commands.Expression analyze(final Lex.List lexes) throws BadSyntax {
        if (lexes.isEmpty()) {
            throw new BadSyntax("Expression is empty");
        }

        var left = Expression4.analyze(lexes);
        while (lexes.is("-") || lexes.is("+")) {
            final var op = lexes.next().text;
            final var right = Expression4.analyze(lexes);
            left = new Operation(op, left, right);
        }
        return left;
    }
}
