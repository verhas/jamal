package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;

import java.util.List;

public class If {


    public static javax0.jamal.prog.commands.If analyze(final Lex.List lexes) throws BadSyntax {
        lexes.next();
        final var condition = Expression.analyze(lexes);
        final var then = Block.analyze(lexes);
        if (lexes.is("else")) {
            lexes.next();
            lexes.eol("Expected newline after else");
            final var otherwise = Block.analyze(lexes);
            return new javax0.jamal.prog.commands.If(condition, then, otherwise);
        }
        if (lexes.is("elseif")) {
            return new javax0.jamal.prog.commands.If(condition, then, new javax0.jamal.prog.commands.Block(List.of(If.analyze(lexes))));
        }
        if (lexes.is("endif")) {
            lexes.next();
            lexes.eol("Expected newline after else");
            return new javax0.jamal.prog.commands.If(condition, then, new javax0.jamal.prog.commands.Block(List.of()));
        }
        throw new BadSyntax("Expected else, elseif or endif after if");
    }
}
