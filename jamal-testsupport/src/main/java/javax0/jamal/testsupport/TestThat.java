package javax0.jamal.testsupport;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.engine.Processor;
import javax0.jamal.engine.UserDefinedMacro;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationTargetException;

/**
 * A simple class that helps the testing of built-in macros.
 * <p>
 * A built-in macro most of the time converts the content to another string. To test that you need an
 * instance of the macro class, a processor, an input object that contains the {@link StringBuilder} and a null
 * reference file name. Then the test just invokes the macro {@link Macro#evaluate(Input, javax0.jamal.api.Processor)}
 * method and checks the returned string with what was expected.
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
 * <p>
 * If and when the macro may throw exception (probably BadSyntaxAt
 */
public class TestThat {
    final private Class<? extends Macro> klass;
    final private Processor processor = new Processor();
    private String input;

    private TestThat(Class<? extends Macro> klass) {
        this.klass = klass;
    }

    /**
     * Create a new instance of the TestThat class.
     *
     * @param klass is the class of the tested macro.
     * @return the testing class
     */
    public static TestThat forMacro(Class<? extends Macro> klass) {
        return new TestThat(klass);
    }

    public TestThat fromInput(String input) {
        this.input = input;
        return this;
    }

    /**
     * Create a new macro, a new processor and test that the input creates the expected output.
     * If they are not the same then JUnit5 assertion failure will happen.
     *
     * @param expected the expected output of the macro
     * @throws NoSuchMethodException     if the macro class can not be instantiated
     * @throws IllegalAccessException    if the macro class can not be instantiated
     * @throws InstantiationException    if the macro class can not be instantiated
     * @throws InvocationTargetException if the macro class can not be instantiated
     * @throws BadSyntaxAt               if the macro evaluation throws BadSyntaxAt
     */
    public void results(String expected) throws
        NoSuchMethodException,
        IllegalAccessException,
        InstantiationException,
        InvocationTargetException,
        BadSyntaxAt, BadSyntax {
        Macro sut = createSut();
        var in = new javax0.jamal.tools.Input(input, null);
        var actual = sut.evaluate(in, processor);
        Assertions.assertEquals(expected, actual);
    }

    private Macro createSut() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        var constructor = klass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /**
     * Checks that the macro throws an exception for a given input.
     *
     * @param throwable the exception we expect
     * @throws NoSuchMethodException     if the macro class can not be instantiated
     * @throws IllegalAccessException    if the macro class can not be instantiated
     * @throws InstantiationException    if the macro class can not be instantiated
     * @throws InvocationTargetException if the macro class can not be instantiated
     */
    public void throwsUp(Class<? extends Throwable> throwable) throws
        NoSuchMethodException,
        IllegalAccessException,
        InstantiationException,
        InvocationTargetException {
        var sut = createSut();
        var in = new javax0.jamal.tools.Input(input, null);
        var processor = new Processor();
        Assertions.assertThrows(throwable, () -> sut.evaluate(in, processor));
    }

    /**
     * Checks that the macro throws a bad syntax exception for the given input.
     *
     * @throws NoSuchMethodException     if the macro class can not be instantiated
     * @throws IllegalAccessException    if the macro class can not be instantiated
     * @throws InstantiationException    if the macro class can not be instantiated
     * @throws InvocationTargetException if the macro class can not be instantiated
     * @throws BadSyntaxAt               should not ever, because this is what we expect to be caught by the invoked
     *                                   {@link #throwsUp(Class)} method, but Java requires that we declre this.
     */
    public void throwsBadSyntax() throws
        NoSuchMethodException,
        IllegalAccessException,
        InstantiationException,
        InvocationTargetException,
        BadSyntaxAt {
        throwsUp(BadSyntaxAt.class);
    }

    /**
     * You can use this method to call to define a global user defined macro for the test in case the tested macro
     * depends on the existence of some user defined macros.
     *
     * @param id         the identifier / name of the macro
     * @param content    the content of the macro
     * @param parameters the list o formal parameters of the macro
     * @return this
     * @throws BadSyntax when the underlying call throws this exception
     */
    public TestThat global(String id, String content, String... parameters) throws BadSyntax {
        var macro = new javax0.jamal.engine.UserDefinedMacro(id, content, parameters);
        processor.getRegister().global(macro);
        return this;
    }

    /**
     * You can use this method to define a global built-in macro. This may be needed when the macro tested
     * needs the services of other macros.
     *
     * @param macro the macro that the tested macro needs for its functioning
     * @return this
     */
    public TestThat global(Macro macro) {
        processor.getRegister().global(macro);
        return this;
    }

    /**
     * You can use this method to define a global built-in macro with an alias. This may be needed when the macro tested
     * needs the services of other macros.
     *
     * @param macro the macro that the tested macro needs for its functioning
     * @param alias the alias name for the macro
     * @return this
     */
    public TestThat global(Macro macro, String alias) {
        processor.getRegister().global(macro, alias);
        return this;
    }

    /**
     * You can use this method to call to define a local user defined macro for the test in case the tested macro
     * depends on the existence of some user defined macros.
     *
     * @param id         the identifier / name of the macro
     * @param content    the content of the macro
     * @param parameters the list o formal parameters of the macro
     * @return this
     * @throws BadSyntax when the underlying call throws this exception
     */
    public TestThat define(String id, String content, String... parameters) throws BadSyntax {
        var macro = new UserDefinedMacro(id, content, parameters);
        processor.getRegister().define(macro);
        return this;
    }

    /**
     * You can use this method to define a local built-in macro. This may be needed when the macro tested
     * needs the services of other macros.
     *
     * @param macro the macro that the tested macro needs for its functioning
     * @return this
     */
    public TestThat define(Macro macro) {
        processor.getRegister().define(macro);
        return this;
    }

    /**
     * You can use this method to define a local built-in macro with an alias. This may be needed when the macro tested
     * needs the services of other macros.
     *
     * @param macro the macro that the tested macro needs for its functioning
     * @param alias the alias name for the macro
     * @return this
     */
    public TestThat define(Macro macro, String alias) {
        processor.getRegister().define(macro, alias);
        return this;
    }
}
