package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.prog.commands.Command;

import java.util.ArrayList;

public class Block {

    public static javax0.jamal.prog.commands.Block analyze(final Lex.List lexes) throws BadSyntax {
        final var commands = new ArrayList<Command>();
        final var block = new javax0.jamal.prog.commands.Block(commands);
        while (lexes.hasNext()) {
            if (lexes.peek().type == Lex.Type.RESERVED) {
                switch(lexes.peek().text){
                    case "\n":
                        lexes.next();
                        break;
                    case "if":
                        commands.add(If.analyze(lexes));
                        break;
                    case "for":
                        commands.add(For.analyze(lexes));
                        break;
                    case "while":
                        commands.add(While.analyze(lexes));
                        break;
                    case "<<":
                        commands.add(Output.analyze(lexes));
                        break;
                    case "next":
                    case "else":
                    case "elseif":
                    case "endif":
                    case "wend":
                        return block;
                    default:
                        throw new BadSyntax("Unexpected reserved word '"+lexes.peek().text+"'");
                }
            }else if( lexes.peek().type == Lex.Type.IDENTIFIER ){
                commands.add(Assignment.analyze(lexes));
            }
        }
        return block;
    }
}
