package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.prog.commands.Constant;
import javax0.jamal.prog.commands.Operation;
import javax0.jamal.prog.commands.Variable;

import static javax0.jamal.prog.analyzer.Expression.getExpressionBetweenParenthese;

/**
 * An expression4 is
 * <p>
 * ( expression )
 * <p>
 * constant
 * variable
 * '+' expression4
 * '-' expression4
 * `@` expression4
 */
public class Expression4 {
    public static javax0.jamal.prog.commands.Expression analyze(final Lex.List lexes) throws BadSyntax {
        if (lexes.isEmpty()) {
            throw new BadSyntax("Expression is empty");
        }
        if (lexes.is("(")) {
            return getExpressionBetweenParenthese(lexes);
        }
        final var lex = lexes.next();
        switch (lex.type) {
            case IDENTIFIER:
                if( lexes.is("(") ){
                    return FunctionCall.analyze(lex, lexes);
                }
                return new Variable(lex.text);
            case STRING:
                return new Constant(lex.text);
            case RESERVED:
                if (lex.text.equals("+")) {
                    return Expression4.analyze(lexes);
                }
                if (lex.text.equals("-")) {
                    return new Operation("-",null,  Expression4.analyze(lexes));
                }
                if (lex.text.equals("!")) {
                    return new Operation("!", null, Expression.analyze(lexes));
                }

                throw new BadSyntax("Expression: expected identifier or string, got " + lex.text);
            default:
                throw new BadSyntax("Expression: expected identifier or string, got " + lex.text);
        }
    }
}
