package javax0.jamal.testsupport;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Macro;
import javax0.jamal.engine.Processor;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationTargetException;

/**
 * A simple class that helps the testing of built-in macros.
 *
 * A built-in macro most of the time converts the content to another string. To test that you need an
 * instance of the macro class, a processor, an input object that contains the StringBuilder and a null
 * reference file name. Then the test just invokes the maco evaluate method and checks the returned string
 * with what was expected.
 * <p>
 * To ease this task you can put this module on the test dependencies and have a test like
 *
 * <pre>
 *     var camelLowerCase = TestThat.forMacro(Camel.LowerCase.class);
 *     camelLowerCase.fromInput("INPUT").results( "input");
 *     camelLowerCase.fromInput("INpUT").results( "input");
 *     camelLowerCase.fromInput("INpuT").results( "input");
 *     camelLowerCase.fromInput("INput").results( "input");
 *     camelLowerCase.fromInput("Input").results( "input");
 *     camelLowerCase.fromInput("input").results( "input");
 *     camelLowerCase.fromInput("IN-PUT").results( "inPut");
 *     camelLowerCase.fromInput("I-N-P-U-T").results( "iNPUT");
 * </pre>
 *
 *
 */
public class TestThat {
    final Class<? extends Macro> klass;

    private TestThat(Class<? extends Macro> klass) {
        this.klass = klass;
    }

    /**
     * Create a new instance of the TestThat class.
     * @param klass is the class of the tested macro.
     * @return the testing class
     */
    public static TestThat forMacro(Class<? extends Macro> klass) {
        return new TestThat(klass);
    }

    private String input;

    public TestThat fromInput(String input){
       this.input = input;
       return this;
    }
    /**
     * Create a new macro, a new processor and test that the input creates the expected output.
     * If they are not the same then JUnit5 assertion failure will happen.
     * @param expected the expected output of the macro
     * @throws NoSuchMethodException if the macro class can not be instantiated
     * @throws IllegalAccessException if the macro class can not be instantiated
     * @throws InstantiationException if the macro class can not be instantiated
     * @throws InvocationTargetException if the macro class can not be instantiated
     * @throws BadSyntax if the macro evaluation throws BadSyntax
     */
    public void results(String expected)
        throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, BadSyntax {
        var sut = klass.getDeclaredConstructor().newInstance();
        var in = new javax0.jamal.tools.Input();
        in.setInput(new StringBuilder(input));
        var processor = new Processor("{", "}");
        var actual = sut.evaluate(in, processor);
        Assertions.assertEquals(expected, actual);
    }

    public void throwsUp(Class<? extends Throwable> throwable)
        throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, BadSyntax {
        var sut = klass.getDeclaredConstructor().newInstance();
        var in = new javax0.jamal.tools.Input();
        in.setInput(new StringBuilder(input));
        var processor = new Processor("{", "}");
        Assertions.assertThrows(throwable, ()-> sut.evaluate(in, processor));
    }

}
