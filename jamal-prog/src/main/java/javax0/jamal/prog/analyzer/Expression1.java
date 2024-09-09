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

public class Expression1 {
    public static javax0.jamal.prog.commands.Expression analyze(final Lex.List lexes) throws BadSyntax {
        if (!lexes.hasNext()) {
            throw new BadSyntax("Expression is empty");
        }
        var left = Expression2.analyze(lexes);
        while (lexes.is("and")) {
            final var op = lexes.next().text;
            final var right = Expression2.analyze(lexes);
            left = new Operation(op, left, right);
        }
        return left;
    }


}
