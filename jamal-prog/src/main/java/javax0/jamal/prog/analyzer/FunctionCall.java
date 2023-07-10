package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.prog.commands.Expression;

public class FunctionCall {
    public static Expression analyze(Lex lex, Lex.List lexes) throws BadSyntax {
        final var function = lex.text;
        final var arguments = MethodCall.analyzeArguments(lexes);
        return new javax0.jamal.prog.commands.FunctionCall(function, arguments.toArray(Expression[]::new));
    }
}
