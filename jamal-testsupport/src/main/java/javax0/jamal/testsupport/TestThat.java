package javax0.jamal.testsupport;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.engine.UserDefinedMacro;
import javax0.jamal.tools.HexDumper;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A simple class that helps the testing of built-in macros.
 * <p>
 * A built-in macro most of the time converts the content to another string. To test that you need an instance of the
 * macro class, a processor, an input object that contains the {@link StringBuilder} and a null reference file name.
 * Then the test just invokes the macro {@link Macro#evaluate(Input, javax0.jamal.api.Processor)} method and checks the
 * returned string with what was expected.
 * <p>
 * To ease this task you can put this module on the test dependencies and have a test like
 *
 * <pre>{@code
 *     var camelLowerCase = TestThat.theMacro(Camel.LowerCase.class);
 *     camelLowerCase.fromTheInput("INPUT").results( "input");
 *     camelLowerCase.fromTheInput("INpUT").results( "input");
 *     camelLowerCase.fromTheInput("INpuT").results( "input");
 *     camelLowerCase.fromTheInput("INput").results( "input");
 *     camelLowerCase.fromTheInput("Input").results( "input");
 *     camelLowerCase.fromTheInput("input").results( "input");
 *     camelLowerCase.fromTheInput("IN-PUT").results( "inPut");
 *     camelLowerCase.fromTheInput("I-N-P-U-T").results( "iNPUT");
 * }</pre>
 * <p>
 * If and when the macro is expected to throw exception (probably BadSyntaxAt) then you can write
 * <pre>
 *     TestThat.forMacro(For.class).fromInput(" x in a,b,c,d= x is either a, b, c or d\n").throwsBadSyntax();
 * </pre>
 * <p>
 * If you expect any other exception, other than {@code BadSyntaxAt} then you can also use {@code
 * throwsUp(exception.class)} instead of {@code throwsBadSyntax()}.
 * <p>
 * You can also define user defined and built-in macros on the outermost and also on the global level.
 * <p>
 * Another possibility to use this class is to
 *
 * <pre>{@code
 * TestThat.theInput("{@define a=alma}{a}").results("alma")
 * }</pre>
 * <p>
 * that invokes not only one macro but rather the whole processing engine.
 */
public class TestThat implements AutoCloseable {
    final private Class<? extends Macro> klass;
    private Processor processor;
    private String input;
    private String macroOpen = "{", macroClose = "}";
    private boolean ignoreLineEndingFlag = false;

    private TestThat(Class<? extends Macro> klass) {
        this.klass = klass;
    }

    public Processor getProcessor() {
        if (processor == null) {
            processor = new Processor(macroOpen, macroClose);
        }
        return processor;
    }

    public TestThat ignoreLineEnding() {
        ignoreLineEndingFlag = true;
        return this;
    }

    /**
     * Create a new instance of the TestThat class.
     *
     * @param klass is the class of the tested macro.
     * @return the testing class
     */
    public static TestThat theMacro(Class<? extends Macro> klass) {
        return new TestThat(klass);
    }

    public static TestThat theInput(String input) {
        final var it = new TestThat(null);
        it.input = input;
        return it;
    }

    public TestThat usingTheSeparators(final String macroOpen, final String macroClose) {
        this.macroOpen = macroOpen;
        this.macroClose = macroClose;
        return this;
    }

    public TestThat fromTheInput(String input) {
        this.input = input;
        return this;
    }

    private Macro createSut() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        var constructor = klass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private Position pos = null;

    public TestThat atPosition(String fileName, int line, int col) {
        pos = new Position(fileName, line, col);
        return this;
    }

    /**
     * Create a new macro, a new processor if needed run it on the input. Return the result of the processing.
     *
     * @return the calculated output
     * @throws NoSuchMethodException     if the macro class can not be instantiated
     * @throws IllegalAccessException    if the macro class can not be instantiated
     * @throws InstantiationException    if the macro class can not be instantiated
     * @throws InvocationTargetException if the macro class can not be instantiated
     * @throws BadSyntaxAt               if the macro evaluation throws BadSyntaxAt
     */
    public String results() throws
        NoSuchMethodException,
        IllegalAccessException,
        InstantiationException,
        InvocationTargetException,
        BadSyntax {
        var in = new javax0.jamal.tools.Input(input, pos);
        if (klass != null) {
            Macro sut = createSut();
            return sut.evaluate(in, getProcessor());
        } else {
            return getProcessor().process(in);
        }
    }

    /**
     * Create a new macro, a new processor if needed run it on the input. Return the result of the processing. Close the
     * processor
     *
     * @return the calculated output
     * @throws NoSuchMethodException     if the macro class can not be instantiated
     * @throws IllegalAccessException    if the macro class can not be instantiated
     * @throws InstantiationException    if the macro class can not be instantiated
     * @throws InvocationTargetException if the macro class can not be instantiated
     * @throws BadSyntaxAt               if the macro evaluation throws BadSyntaxAt
     */
    private String resultsClose() throws
        InvocationTargetException,
        NoSuchMethodException,
        InstantiationException,
        IllegalAccessException,
        BadSyntax {
        final var output = results();
        close();
        return output;
    }

    /**
     * Create a new macro, a new processor and test that the input creates the expected output. If they are not the same
     * then JUnit5 assertion failure will happen.
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
        BadSyntax {
        if (ignoreLineEndingFlag) {
            final var expectedNl = expected.replaceAll("\r","");
            final var resultNl = resultsClose().replaceAll("\r","");
            Assertions.assertEquals(expectedNl, resultNl);
        } else {
            Assertions.assertEquals(expected, resultsClose());
        }
    }

    /**
     * The same as {@link #results()} but converts the UTF-8 bytes of the expected and actual string to hexa string
     * before comparing. This will show the diff in hex format in case there is any and that way it is easier to find
     * the actual difference in case of non-printable characters differ.
     *
     * @param expected the expected output of the macro
     * @throws NoSuchMethodException     if the macro class can not be instantiated
     * @throws IllegalAccessException    if the macro class can not be instantiated
     * @throws InstantiationException    if the macro class can not be instantiated
     * @throws InvocationTargetException if the macro class can not be instantiated
     * @throws BadSyntaxAt               if the macro evaluation throws BadSyntaxAt
     */
    public void resultsBin(String expected) throws
        NoSuchMethodException,
        IllegalAccessException,
        InstantiationException,
        InvocationTargetException,
        BadSyntax {
        Assertions.assertEquals(HexDumper.encode(expected.getBytes(StandardCharsets.UTF_8)), HexDumper.encode(resultsClose().getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Create a new macro, a new processor and test that the input creates the expected output. If it is not matched
     * then JUnit5 assertion failure will happen.
     *
     * @param expected the expected output of the macro
     * @throws NoSuchMethodException     if the macro class can not be instantiated
     * @throws IllegalAccessException    if the macro class can not be instantiated
     * @throws InstantiationException    if the macro class can not be instantiated
     * @throws InvocationTargetException if the macro class can not be instantiated
     * @throws BadSyntaxAt               if the macro evaluation throws BadSyntaxAt
     */
    public void matches(String expected) throws
        NoSuchMethodException,
        IllegalAccessException,
        InstantiationException,
        InvocationTargetException,
        BadSyntax {
        Assertions.assertTrue(Pattern.compile(expected).matcher(resultsClose()).matches());
    }


    /**
     * Same as calling {@link #throwsUp(Class, String) throwsUp(Class,null)}
     *
     * @param throwable see {@link #throwsUp(Class, String)}
     * @throws NoSuchMethodException     see {@link #throwsUp(Class, String)}
     * @throws IllegalAccessException    see {@link #throwsUp(Class, String)}
     * @throws InstantiationException    see {@link #throwsUp(Class, String)}
     * @throws InvocationTargetException see {@link #throwsUp(Class, String)}
     * @throws BadSyntax                 see {@link #throwsUp(Class, String)}
     */
    public void throwsUp(Class<? extends Throwable> throwable) throws
        NoSuchMethodException,
        IllegalAccessException,
        InstantiationException,
        InvocationTargetException, BadSyntax {
        throwsUp(throwable, null);
    }

    /**
     * Checks that the macro throws an exception for a given input. It also checks that a message in the exception
     * matches the regular expression.
     * <p>
     * Notes:
     * <ul>
     *     <li>The regular exception should match the whole message, not only a part of it. If needed put {@code .*}
     *     before and after the pattern to match only a part.
     *     <li>The {@code filename/Line:Column} location part is chopped off the message before trying to match. You
     *     should use a regular expression that matches the string that you created where throwing it.
     *     <li>The matching process tries to match the regular expression against the message of the exception as well
     *     as against the message of the causing exception and against the suppressed exceptions. If any matches the
     *     result is accepted.
     *     <li>Looking for the causing exception and the suppressed exceptions is recursive. For example if the regular
     *     expression matches the message of the causing exception of one of the suppressed exceptions it will be okay.
     * </ul>
     * <p>
     * If the regular expression does not match any of the above possibilities then an assertion fail will execute.
     *
     * @param throwable the exception we expect
     * @param regex     a regular expression to match the message of the exception against. Not checked if {@code
     *                  null}.
     * @throws NoSuchMethodException     if the macro class can not be instantiated
     * @throws IllegalAccessException    if the macro class can not be instantiated
     * @throws InstantiationException    if the macro class can not be instantiated
     * @throws InvocationTargetException if the macro class can not be instantiated
     * @throws BadSyntax                 if the evaluation throws BadSyntax but some other type is expected
     */
    public void throwsUp(Class<? extends Throwable> throwable, String regex) throws
        NoSuchMethodException,
        IllegalAccessException,
        InstantiationException,
        InvocationTargetException, BadSyntax {
        try {
            results();
            Assertions.fail("The evaluation did not throw an exception");
        } catch (Throwable t) {
            if (throwable.isAssignableFrom(t.getClass())) {
                if (regex != null) {
                    final var pattern = Pattern.compile(regex, Pattern.DOTALL);
                    if (matches(pattern, t)) {
                        return;
                    }
                    Assertions.fail("The evaluation did throw an exception but " +
                        "the exception message did not match the pattern '" + regex + "'\n" +
                        "Possible patterns are:\n" +
                        String.join("\n", collect(t)), t);
                }
            } else {
                throw t;
            }
        } finally {
            close();
        }
    }

    private static String myQuote(String string) {
        return string.replaceAll("(\\.|\\\\|\\(|\\)|\\{|\\}|\\|)", "\\\\$1");
    }

    private static Set<String> collect(final Throwable t) {
        if (t == null) {
            return Set.of();
        }
        final var retval = new HashSet<String>();
        retval.add(myQuote(t.getMessage()));
        retval.addAll(collect(t.getCause()));
        for (final var s : t.getSuppressed()) {
            retval.addAll(collect(s));
        }
        return retval;
    }

    /**
     * Check that the pattern matches the message of the exception, or the causing exception or any supressed exception.
     * It also checks that the causing or suppressed exception is not the same as the original and the check is done in
     * a recursive call.
     * <p>
     * This simple algorithm may run into an infinite loop if the data structure is recursive and has a loop longer than
     * one. E.g.: cause of A is B, and cause of B is A.
     *
     * @param pattern the pattern to match
     * @param t       the exception to check against the pattern
     * @return true if the exception of any causing or suppressed exception message matches the pattern
     */
    private static boolean matches(final Pattern pattern, final Throwable t) {
        return t != null &&
            (matches(pattern, t.getMessage())
                || (t != t.getCause() && matches(pattern, t.getCause()))
                || Arrays.stream(t.getSuppressed()).anyMatch(sup -> sup != t && matches(pattern, sup)))
            ;
    }

    /**
     * Checks that the string {@code s}. which is the message of an exception matches the pattern. If there is a "{@code
     * at file/1:15}" location info at the end of the string it is chopped off before matching.
     *
     * @param pattern the pattern to match against
     * @param s       the string to match
     * @return true of the string matches
     */
    private static boolean matches(final Pattern pattern, final String s) {
        final var index = s.lastIndexOf(" at");
        return pattern.matcher(s.substring(0, index == -1 ? s.length() : index)).matches();
    }

    /**
     * Same as {@link #throwsBadSyntax(String) throwsBadSyntax(null)}.
     *
     * @throws NoSuchMethodException     see {@link #throwsBadSyntax(String)}
     * @throws IllegalAccessException    see {@link #throwsBadSyntax(String)}
     * @throws InstantiationException    see {@link #throwsBadSyntax(String)}
     * @throws InvocationTargetException see {@link #throwsBadSyntax(String)}
     * @throws BadSyntax                 see {@link #throwsBadSyntax(String)}
     */
    public void throwsBadSyntax() throws
        NoSuchMethodException,
        IllegalAccessException,
        InstantiationException,
        InvocationTargetException, BadSyntax {
        throwsBadSyntax(null);
    }

    /**
     * Checks that the macro throws a bad syntax exception for the given input.
     *
     * @param regex the message of the exception should match the regular expression.
     * @throws NoSuchMethodException     if the macro class can not be instantiated
     * @throws IllegalAccessException    if the macro class can not be instantiated
     * @throws InstantiationException    if the macro class can not be instantiated
     * @throws InvocationTargetException if the macro class can not be instantiated
     * @throws BadSyntax                 never happens
     */
    public void throwsBadSyntax(String regex) throws
        NoSuchMethodException,
        IllegalAccessException,
        InstantiationException,
        InvocationTargetException, BadSyntax {
        throwsUp(BadSyntax.class, regex);
    }

    /**
     * You can use this method to call to define a global user defined macro for the test in case the tested macro
     * depends on the existence of some user defined macros.
     *
     * @param id         the identifier / name of the macro
     * @param content    the content of the macro
     * @param parameters the list o formal parameters of the macro
     * @return {@code this}
     * @throws BadSyntax when the underlying call throws this exception
     */
    public TestThat global(String id, String content, String... parameters) throws BadSyntax {
        var macro = new javax0.jamal.engine.UserDefinedMacro(getProcessor(), id, content, parameters);
        getProcessor().getRegister().global(macro);
        return this;
    }

    /**
     * You can use this method to define a global built-in macro. This may be needed when the macro tested needs the
     * services of other macros.
     *
     * @param macro the macro that the tested macro needs for its functioning
     * @return {@code this}
     */
    public TestThat global(Macro macro) {
        getProcessor().getRegister().global(macro);
        return this;
    }

    /**
     * You can use this method to define a global built-in macro with an alias. This may be needed when the macro tested
     * needs the services of other macros.
     *
     * @param macro the macro that the tested macro needs for its functioning
     * @param alias the alias name for the macro
     * @return {@code this}
     */
    public TestThat global(Macro macro, String alias) {
        getProcessor().getRegister().global(macro, alias);
        return this;
    }

    /**
     * You can use this method to call to define a local user defined macro for the test in case the tested macro
     * depends on the existence of some user defined macros.
     *
     * @param id         the identifier / name of the macro
     * @param content    the content of the macro
     * @param parameters the list o formal parameters of the macro
     * @return {@code this}
     * @throws BadSyntax when the underlying call throws this exception
     */
    public TestThat define(String id, String content, String... parameters) throws BadSyntax {
        var macro = new UserDefinedMacro(getProcessor(), id, content, parameters);
        getProcessor().getRegister().define(macro);
        return this;
    }

    /**
     * You can use this method to define a local built-in macro. This may be needed when the macro tested needs the
     * services of other macros.
     *
     * @param macro the macro that the tested macro needs for its functioning
     * @return {@code this}
     */
    public TestThat define(Macro macro) {
        getProcessor().getRegister().define(macro);
        return this;
    }

    /**
     * You can use this method to define a local built-in macro with an alias. This may be needed when the macro tested
     * needs the services of other macros.
     *
     * @param macro the macro that the tested macro needs for its functioning
     * @param alias the alias name for the macro
     * @return {@code this}
     */
    public TestThat define(Macro macro, String alias) {
        getProcessor().getRegister().define(macro, alias);
        return this;
    }

    @Override
    public void close() {
        getProcessor().close();
    }
}
