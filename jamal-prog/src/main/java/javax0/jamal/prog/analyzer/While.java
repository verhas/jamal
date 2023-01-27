package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;

public class While {


    public static javax0.jamal.prog.commands.While analyze(final Lex.List lexes) throws BadSyntax {
        lexes.next();
        final var condition = Expression.analyze(lexes);
        lexes.eol("New line is expected after 'while expression'");
        final var block = Block.analyze(lexes);
        lexes.assume(Lex.Type.RESERVED, "wend", "Expected wend after while");
        lexes.eol("Expected newline after wend");
        return new javax0.jamal.prog.commands.While(condition, block);
    }

}
