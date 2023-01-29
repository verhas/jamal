package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.param.StringFetcher;

import java.util.ArrayList;
import java.util.Set;

public class Lexer {

    final static private Set<String> RESERVED = Set.of(
            "if", "else", "elseif", "then", "endif", "while", "wend", "for", "next", "do", "until", "and", "or", "not", "to", "step"
    );
    final static private String[] OPERANDS = {
            "==", "!=", "<=", ">=", "<<", "!", "=", "+", "-", "*", "/", "%", "(", ")", "<", ">",
    };

    public Lex.List analyze(Input in) throws BadSyntax {
        final var list = new ArrayList<Lex>();
        while (!in.isEmpty()) {
            if (Character.isWhitespace(in.charAt(0)) && in.charAt(0) != '\n') {
                InputHandler.skip(in, 1);
                continue;
            }
            if (in.charAt(0) == '\n') {
                final var lex = new Lex(Lex.Type.RESERVED, "\n");
                list.add(lex);
                InputHandler.skip(in, 1);
                continue;
            }
            if (Macro.validId1stChar(in.charAt(0))) {
                final var id = InputHandler.fetchId(in);
                if (RESERVED.contains(id)) {
                    final var lex = new Lex(Lex.Type.RESERVED, id);
                    list.add(lex);
                } else {
                    final var lex = new Lex(Lex.Type.IDENTIFIER, id);
                    list.add(lex);
                }
                continue;
            }
            if (in.charAt(0) == '"') {
                final var str = StringFetcher.getString(in);
                final var lex = new Lex(Lex.Type.STRING, str);
                list.add(lex);
                continue;
            }
            if (Character.isDigit(in.charAt(0))) {
                final var str = InputHandler.fetchNumber(in);
                final var lex = new Lex(Lex.Type.STRING, str);
                list.add(lex);
                continue;
            }
            int operandIndex = InputHandler.startsWith(in, OPERANDS);
            if (operandIndex >= 0) {
                final var lex = new Lex(Lex.Type.RESERVED, OPERANDS[operandIndex]);
                list.add(lex);
                InputHandler.skip(in, OPERANDS[operandIndex].length());
                continue;
            }
            throw new BadSyntax("Unexpected character '" + in.charAt(0) + "' in the input");
        }
        return new Lex.List(list);
    }
}
