package javax0.jamal.prog;

import javax0.jamal.engine.Processor;
import javax0.jamal.prog.analyzer.Block;
import javax0.jamal.prog.analyzer.Lexer;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestProgram {

    private void test(final String input, final String expected) throws Exception {
        test(input, expected, Map.of());
    }

    private void test(final String input, final String expected, final Map<String, String> parameters) throws Exception {
        final var processor = new Processor();
        for (final var entry : parameters.entrySet()) {
            processor.newUserDefinedMacro(entry.getKey(), entry.getValue());
        }
        final var block = Block.analyze(new Lexer().analyze(Input.makeInput(input)));
        Assertions.assertEquals(expected, block.execute(processor));
    }

    @DisplayName("Test various programs")
    @Test
    void test() throws Exception {
        test("i = 55\n" +
                "while i < 58\n" +
                "<< \"a\"\n" +
                "i = i + 1\n" +
                "wend\n", "aaa");

        test("<< \"alma\"", "alma");
        test("for i=1 to 3\n" +
                        "<< \"a\"\n" +
                        "next",
                "aaa");

    }

}
