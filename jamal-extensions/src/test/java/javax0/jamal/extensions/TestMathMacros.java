package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestMathMacros {


    @Test
    void testPlus() throws Exception {
        final var sut = TestThat.theMacro(MathMacros.Plus.class);
        sut.fromTheInput("1 1 1").results("3");
        sut.fromTheInput("1 1 1.3E2").results("132");
    }

    @Test
    void testMult() throws Exception {
        final var sut = TestThat.theMacro(MathMacros.Mult.class);
        sut.fromTheInput("1 1 1").results("1");
        sut.fromTheInput("1 1 1.3E2").results("130");
        sut.fromTheInput("1 2 3 4 5 6 7 8").results("40320");
    }

    @Test
    void testSub() throws Exception {
        final var sut = TestThat.theMacro(MathMacros.Sub.class);
        sut.fromTheInput("1 1 1").results("-1");
        sut.fromTheInput("1 1 1.3E2").results("-130");
        sut.fromTheInput("1 2 3 4 5 6 7 8").results("-34");
    }

    @Test
    void testDiv() throws Exception {
        final var sut = TestThat.theMacro(MathMacros.Div.class);
        sut.fromTheInput("10 1 1").results("8");
        sut.fromTheInput("10 0x1 1.5").results("7.5");
    }

    @Test
    void testLessThan() throws Exception {
        final var sut = TestThat.theMacro(MathMacros.LessThan.class);
        sut.fromTheInput("1 2 3 4").results("true");
        sut.fromTheInput("1 2 3 3").results("false");
    }

    @Test
    void testLessThanOrEqual() throws Exception {
        final var sut = TestThat.theMacro(MathMacros.Le.class);
        sut.fromTheInput("1 2 3 4").results("true");
        sut.fromTheInput("1 2 3 3").results("true");
    }

}
