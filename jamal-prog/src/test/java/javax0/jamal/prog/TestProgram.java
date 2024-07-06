package javax0.jamal.prog;

import javax0.jamal.engine.Processor;
import javax0.jamal.prog.analyzer.Block;
import javax0.jamal.prog.analyzer.Lexer;
import javax0.jamal.prog.commands.Context;
import javax0.jamal.testsupport.TestThat;
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
        Assertions.assertEquals(expected, block.execute(new Context(processor)));
    }

    @DisplayName("Test various programs")
    @Test
    void test() throws Exception {
        test("i = 55\n" +
                "while i < 58 'this is a comment\n" +
                "rem this is a full line comment\n" +
                "<< \"a\" rem this is also comment\n" +
                "i = i + 1\n" +
                "wend\n", "aaa");

        test("<< \"alma\"", "alma");
        test("for i=1 to 3\n" +
                        "<< \"a\"\n" +
                        "next",
                "aaa");

    }

    @DisplayName("Test various programs")
    @Test
    void test1() throws Exception {
        TestThat.theInput("{@define a(x)=13 x}\n" +
                "{@define b   =13 x}\n" +
                "{@try! {@program\n" +
                "<<a}}\n" +
                "{@program\n" +
                "<<b\n" +
                "evil = 666\n" +
                "}\n" +
                "jajjj -{evil}-").results("\n" +
                "\n" +
                "Macro 'a' needs 1 arguments and got 0\n" +
                "13 x\n" +
                "jajjj -666-");
    }

    @DisplayName("Test various programs")
    @Test
    void test2() throws Exception {
        TestThat.theInput("" +
                "{@program stepLimit=5\n" +
                "      for i=1 to 10\n" +
                "          <<i\n" +
                "      next\n" +
                "}"
        ).throwsBadSyntax("Step limit reached");
    }


    @DisplayName("Avoids infinite loop for a command that is simply a String")
    @Test
    void test3() throws Exception {
        TestThat.theInput("" +
                "{@program \n" +
                "\"a\"\n" +
                "}"
        ).throwsBadSyntax("Unexpected token 'a'");
    }

    @DisplayName("elseif works also as 'else' 'if' in two words")
    @Test
    void testElseIf() throws Exception {
        TestThat.theInput("" +
                "{@program \n" +
                "if 1==1 then\n" +
                "    <<\"a\"\n" +
                "else if 1== 1 then\n" +
                "    <<\"b\"\n" +
                "  else\n" +
                "    <<\"c\"\n" +
                "end if\n" +
                "}"
        ).results("a");
    }

    @DisplayName("Test floating point execution")
    @Test
    void testFloat() throws Exception {
        TestThat.theInput("" +
                "{@program float scale=4\n" +
                "a = 13.2\n" +
                "b = 14.456\n" +
                "    <<a / b \n" +
                "    <<\"\\n\"\n" +
                "    << 1 / 3\n" +
                "    <<\"\\n\"\n" +
                "    << 1 * 3\n" +
                "    <<\"\\n\"\n" +
                "    << 1 + 3\n" +
                "    <<\"\\n\"\n" +
                "    << 1 - 3\n" +
                "    <<\"\\n\"\n" +
                "    << 1 % 3\n" +
                "}"
        ).results("0.9131\n" +
                "0.3333\n" +
                "3\n" +
                "4\n" +
                "-2\n" +
                "1");
    }

}
