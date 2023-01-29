package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;

public class While {


    public static javax0.jamal.prog.commands.While analyze(final Lex.List lexes) throws BadSyntax {
        lexes.next();
        final var condition = Expression.analyze(lexes);
        lexes.eol("New line is expected after 'while expression'");
        final var block = Block.analyze(lexes);
        lexes.assumeKWNL( "wend", "Expected wend after while","Expected newline after wend");
        return new javax0.jamal.prog.commands.While(condition, block);
    }

}
