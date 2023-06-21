package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.prog.commands.Expression;

import java.util.ArrayList;
import java.util.List;

public class MethodCall {
    public static Expression analyze(Expression thisValue, Lex.List lexes) throws BadSyntax {
        BadSyntax.when(!lexes.hasNext(), "Method call: expected method name after '.'");
        final var function = lexes.next().text;
        BadSyntax.when(!lexes.is("("), "Method call: expected '(' after the method name '%s", function);
        final var arguments = analyzeArguments(lexes);
        arguments.add(thisValue);
        return new javax0.jamal.prog.commands.FunctionCall(function, arguments.toArray(Expression[]::new));
    }

    static List<Expression> analyzeArguments(Lex.List lexes) throws BadSyntax {
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
        return arguments;
    }
}
