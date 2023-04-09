package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;

import java.util.List;

public class If {


    public static javax0.jamal.prog.commands.If analyze(final Lex.List lexes) throws BadSyntax {
        lexes.next();
        final var condition = Expression.analyze(lexes);
        lexes.assume(Lex.Type.RESERVED, "then", "Expected then after if condition");
        lexes.eol("Expected newline after if condition");
        final var then = Block.analyze(lexes);
        if (lexes.is("elseif")) {
            return new javax0.jamal.prog.commands.If(condition, then, new javax0.jamal.prog.commands.Block(List.of(If.analyze(lexes))));
        }
        if (lexes.is("else")) {
            lexes.next();
            if( lexes.is("if") ){
                return new javax0.jamal.prog.commands.If(condition, then, new javax0.jamal.prog.commands.Block(List.of(If.analyze(lexes))));
            }
            lexes.eol("Expected newline after else");
            final var otherwise = Block.analyze(lexes);
            final var res = new javax0.jamal.prog.commands.If(condition, then, otherwise);
            lexes.assumeEndKWNL("if", "Expected endif after else block","Expected newline after endif");
            return res;
        }
        lexes.assumeEndKWNL("if", "Expected else, elseif or endif after if", "Expected newline after endif");
        return new javax0.jamal.prog.commands.If(condition, then, new javax0.jamal.prog.commands.Block(List.of()));
    }
}
