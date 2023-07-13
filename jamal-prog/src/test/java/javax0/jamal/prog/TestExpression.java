package javax0.jamal.prog;

import javax0.jamal.engine.Processor;
import javax0.jamal.prog.analyzer.Expression;
import javax0.jamal.prog.analyzer.Lexer;
import javax0.jamal.prog.commands.Assignment;
import javax0.jamal.prog.commands.Context;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestExpression {

    private void test(final String input, final String expected) throws Exception {
        test(input, expected, Map.of());
    }

    private void test(final String input, final String expected, final Map<String, String> parameters) throws Exception {
        final var processor = new Processor();
        for (final var entry : parameters.entrySet()) {
            Assignment.let(processor,entry.getKey(), entry.getValue());
        }
        final var expression = Expression.analyze(new Lexer().analyze(Input.makeInput(input)));
        Assertions.assertEquals(expected, expression.execute(new Context(processor)));
    }

    @DisplayName("Test various constant expressions")
    @Test
    void constantIsAValidExpression() throws Exception {
        test("1 < 2", "true");
        test("113", "113");
        test("1+1", "2");
        test("1-1", "0");
        test("1*1", "1");
        test("1/1", "1");
        test("1%1", "0");
        test("1+ 1", "2");
        test("1- 1", "0");
        test("1* 1", "1");
        test("1/ 1", "1");
        test("1% 1", "0");
        test("1 + 1 *3", "4");
        test("(1 + 1 )*3", "6");
        test("(1 + 1/2 )*3", "3");
        test("(1 + 1/2 )*3 and 0", "false");
        test("not(1 + 1/2 )*3 and 0", "false");
        test( "+13", "13");
        test( "-13", "-13");
        test( "- \"!\"", "-!");
    }
}
