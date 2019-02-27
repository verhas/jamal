package javax0.jamal.testsupport;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.lang.reflect.InvocationTargetException;

public class TestThatTest {

    @Test
    @DisplayName("forMacro returns a TestThat instance")
    void testConstructrs() {
        Assertions.assertEquals(TestThat.class, TestThat.forMacro(Macro.class).getClass());
    }

    @Test
    @DisplayName("TestThat asserts the result OK")
    void testResultOK() throws
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            BadSyntax,
            IllegalAccessException {
        TestThat.forMacro(TestingMacro.class).fromInput("").results(null);
    }

    @Test
    @DisplayName("TestThat asserts fails when output does not match")
    void testResultFailure() {
        Assertions.assertThrows(AssertionFailedError.class,
                () -> TestThat.forMacro(TestingMacro.class).fromInput("").results(""));
    }

    @Test
    @DisplayName("TestThat tests macro throwing exception")
    void testThrowingMacro()throws
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            BadSyntax,
            IllegalAccessException {
        TestThat.forMacro(TestingThrowingMacro.class).fromInput("").throwsUp(BadSyntax.class);
    }

    @Test
    @DisplayName("TestThat tests macro checks BadSyntax properly")
    void testBadSyntaxMacro()throws
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            BadSyntax,
            IllegalAccessException {
        TestThat.forMacro(TestingThrowingMacro.class).fromInput("").throwsBadSyntax();
    }

    private static class TestingMacro implements Macro {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            return null;
        }
    }

    private static class TestingThrowingMacro implements Macro {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            throw new BadSyntax("test bad syntax");
        }
    }
}
