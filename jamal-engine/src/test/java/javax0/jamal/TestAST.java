package javax0.jamal;

import javax0.jamal.api.ASTNode;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
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
        final var input = "{#comment {@eval {   #include res:.jim}{ @ident zaba}}baba}";
        final var output = processor.process(new Input(input, new Position("myFile.jam")));
        processor.close();
        final var ast = processor.getAST();
        Assertions.assertEquals("", output);
        Assertions.assertEquals("  list[0..59]\n" +
                " \"{#comment {@eval {   #include res:.jim}{ @ident zaba}}baba}\"\n" +
                "---+  macro[0..59]\n" +
                "---+ \"{#comment {@eval {   #include res:.jim}{ @ident zaba}}baba}\"\n" +
                "---+---+  open[0..1]\n" +
                "---+---+ \"{\"\n" +
                "---+---+  text[1..10]\n" +
                "---+---+ \"#comment \"\n" +
                "---+---+  macro[10..54]\n" +
                "---+---+ \"{@eval {   #include res:.jim}{ @ident zaba}}\"\n" +
                "---+---+---+  open[10..11]\n" +
                "---+---+---+ \"{\"\n" +
                "---+---+---+  macro[17..39]\n" +
                "---+---+---+ \"{   #include res:.jim}\"\n" +
                "---+---+---+---+  open[17..18]\n" +
                "---+---+---+---+ \"{\"\n" +
                "---+---+---+---+  close[38..39]\n" +
                "---+---+---+---+ \"}\"\n" +
                "---+---+---+  macro[39..53]\n" +
                "---+---+---+ \"{ @ident zaba}\"\n" +
                "---+---+---+---+  open[39..40]\n" +
                "---+---+---+---+ \"{\"\n" +
                "---+---+---+---+  close[38..39]\n" +
                "---+---+---+---+ \"}\"\n" +
                "---+---+  text[54..58]\n" +
                "---+---+ \"baba\"\n" +
                "---+---+  close[58..59]\n" +
                "---+---+ \"}\"\n", toString(ast, input));
    }

    @Test
    @DisplayName("buildAST for asingle user defined macro")
    void buildAST_singleUDMacro() throws Exception {
        final var processor = new Processor("{", "}",null,true);
        final var input = "{a {b}}";
        try {
            processor.process(new Input(input, new Position("myFile.jam")));
        } catch (final Exception ignored) {
            // it will throw an exception, but the AST should be built before that
            // may be partial, though
        }
        processor.close();
        final var ast = processor.getAST();
        Assertions.assertEquals("  list[0..3]\n" +
                " \"{a}\"\n" +
                "---+  macro[0..3]\n" +
                "---+ \"{a}\"\n" +
                "---+---+  open[0..1]\n" +
                "---+---+ \"{\"\n" +
                "---+---+  close[2..3]\n" +
                "---+---+ \"}\"\n", toString(ast, input));
    }

    @Test
    @DisplayName("build the AST and display it for recursive definition")
    void buildAST_recursive() throws Exception {
        final var processor = new Processor("{", "}",null,true);
        final var input = "{{{a}}}";
        try {
            final var output = processor.process(new Input(input, new Position("myFile.jam")));
        } catch (final Exception e) {
        }
        processor.close();
        final var ast = processor.getAST();
        //Assertions.assertEquals("2", output);
        Assertions.assertEquals("  list[0..7]\n" +
                " \"{{{a}}}\"\n" +
                "---+  macro[0..7]\n" +
                "---+ \"{{{a}}}\"\n" +
                "---+---+  open[0..1]\n" +
                "---+---+ \"{\"\n" +
                "---+---+  close[4..5]\n" +
                "---+---+ \"}\"", toString(ast, input));
    }

}
