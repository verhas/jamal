package javax0.jamal;

import javax0.jamal.api.ASTNode;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestAST {

    private static String toString(ASTNode root, String input) {
        return toString(root, 0, input);
    }

    private static String toString(ASTNode root, int level, String input) {
        final var sb = new StringBuilder();
        sb.append("---+".repeat(level)).append("  ").append(root.type()).append("[")
                .append(root.start()).append("..").append(root.end()).append("]")
                .append("\n");
        sb.append("---+".repeat(level)).append(" \"")
                .append(input, root.start(), root.end())
                .append("\"\n");
        for (final var child : root.children()) {
            sb.append(toString(child, level + 1, input));
        }
        return sb.toString();
    }

    @Test
    @DisplayName("build the AST and display it")
    void buildAST_simple() throws Exception {
        final var processor = new Processor("{", "}",null,true);
        final var input = "{#comment {@eval {   @include res:.jim}{@ident zaba}}baba}";
        final var output = processor.process(new Input(input, new Position("myFile.jam")));
        processor.close();
        final var ast = processor.getAST();
        System.out.println(output);
        System.out.println(toString(ast, input));
    }

}
