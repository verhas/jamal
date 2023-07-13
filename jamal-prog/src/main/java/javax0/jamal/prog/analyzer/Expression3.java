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
public class Expression3 {
    public static javax0.jamal.prog.commands.Expression analyze(final Lex.List lexes) throws BadSyntax {
        if (lexes.isEmpty()) {
            throw new BadSyntax("Expression is empty");
        }
        var expression4 = Expression4.analyze(lexes);
        while( lexes.is(".")){
            lexes.next(); // consume the '.'
            expression4 = MethodCall.analyze(expression4, lexes);
        }
        if (lexes.is("*") || lexes.is("/") || lexes.is("%")) {
            final var op = lexes.next().text;
            return new Operation(op, expression4, Expression3.analyze(lexes));
        }
        return expression4;
    }
}
