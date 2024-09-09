package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.prog.commands.Operation;

/**
 * An expression3 is
 * <pre>
 * expression4 '*' expression3
 * expression4 '/' expression3
 * expression4 '%' expression3
 * expression4
 * </pre>
 */
public class Expression4 {
    public static javax0.jamal.prog.commands.Expression analyze(final Lex.List lexes) throws BadSyntax {
        if (lexes.isEmpty()) {
            throw new BadSyntax("Expression is empty");
        }
        var left = Expression5.analyze(lexes);
        while( lexes.is(".")){
            lexes.next(); // consume the '.'
            left = MethodCall.analyze(left, lexes);
        }
        while (lexes.is("*") || lexes.is("/") || lexes.is("%")) {
            final var op = lexes.next().text;
            final var right = Expression5.analyze(lexes);
            left = new Operation(op, left, right);
        }
        return left;
    }
}
