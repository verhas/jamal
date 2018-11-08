package javax0.jamal.testsupport;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Macro;
import javax0.jamal.engine.Processor;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationTargetException;

public class TestMacro {
    final Class<? extends Macro> klass;

    private TestMacro(Class<? extends Macro> klass) {
        this.klass = klass;
    }

    public static TestMacro forMacro(Class<? extends Macro> klass) {
        return new TestMacro(klass);
    }

    public void test(String input, String expected)
        throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, BadSyntax {
        var sut = klass.getDeclaredConstructor().newInstance();
        var in = new javax0.jamal.tools.Input();
        in.setInput(new StringBuilder(input));
        var processor = new Processor("{", "}");
        var actual = sut.evaluate(in, processor);
        Assertions.assertEquals(expected, actual);
    }
}
