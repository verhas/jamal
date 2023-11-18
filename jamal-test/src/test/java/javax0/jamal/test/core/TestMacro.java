package javax0.jamal.test.core;

import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Test the built-in macro "macro".
 * <p>
 * A few test cases are defined in the test file TestMacro.jyt, but most of the tests need some special user defined
 * macro that cannot be registered from a Jamal source, and that way these tests can only be written in Java.
 */
public class TestMacro {

    /**
     * A user defined macro that has a non-standard name, and therefore makes sense aliasing it in the tests.
     */
    final Identified ud = new UserDefinedMacro() {

        @Override
        public String getId() {
            return "+++";
        }

        @Override
        public String evaluate(final String... parameters) {
            if (parameters.length == 0) {
                return "no param";
            }
            return "" + Arrays.stream(parameters).mapToInt(Integer::parseInt).sum();
        }
    };

    final Macro builtIn = new Macro() {
        @Override
        public String evaluate(final Input in, final Processor processor) {
            if (in.length() == 0) {
                return "no param";
            }
            return Arrays.stream(in.toString().split(",", -1)).map(String::trim).mapToInt(Integer::parseInt).sum() + "";
        }

        @Override
        public String getId() {
            return "not an identifier";
        }
    };

    @Test
    @DisplayName("Get the user defined macro name")
    void testUdName() throws Exception {

        final var test = TestThat.theInput("{@macro +++}");
        test.getProcessor().getRegister().define(ud);
        test.results("no param");
    }

    @Test
    @DisplayName("Get the user defined macro with alias")
    void testUdAlias() throws Exception {
        final var test = TestThat.theInput("{{@macro [alias] +++}/1/2/3}");
        test.getProcessor().getRegister().define(ud);
        test.results("6");
    }

    @Test
    @DisplayName("Get the user defined macro with given alias")
    void testUdAliasDefined1() throws Exception {
        final var test = TestThat.theInput("{{@macro [alias=add] +++}/1/2/3}{add/1/2/3}{add/3/3}");
        test.getProcessor().getRegister().define(ud);
        test.results("666");
    }

    @Test
    @DisplayName("Get the user defined macro with given alias in block")
    void testUdAliasDefined2() throws Exception {
        final var test = TestThat.theInput("{#block {@macro [alias=add] +++}}{add/1/2/3}{add/3/3}");
        test.getProcessor().getRegister().define(ud);
        test.results("66");
    }

    @Test
    @DisplayName("Get the user defined macro with given alias global")
    void testUdAliasDefinedGlobal() throws Exception {
        final var test = TestThat.theInput("{#block {@macro [alias=:add] +++}}{add/1/2/3}{add/3/3}");
        test.getProcessor().getRegister().define(ud);
        test.results("66");
    }

    @Test
    @DisplayName("Get the built in macro name")
    void testBuiltInName() throws Exception {

        final var test = TestThat.theInput("{@macro [type=builtin]not an identifier}");
        test.getProcessor().getRegister().define(builtIn);
        test.results("no param");
    }

    @Test
    @DisplayName("Get the built in macro with alias")
    void testBuiltInAlias() throws Exception {
        final var test = TestThat.theInput("{#{@macro [alias type=built-in] not an identifier} 1,2,3}");
        test.getProcessor().getRegister().define(builtIn);
        test.results("6");
    }

    @Test
    @DisplayName("Get the built in macro with given alias")
    void testBuiltInAliasDefined() throws Exception {
        final var test = TestThat.theInput(
                "{#{@macro [alias=add type=\"built in\"] not an identifier} 1,2,3}{@add 1,2,3}{@add 3,3}");
        test.getProcessor().getRegister().define(builtIn);
        test.results("666");
    }

    @Test
    @DisplayName("Get the built in macro with given alias in block")
    void testBuiltInAliasDefined2() throws Exception {
        final var test = TestThat.theInput(
                "{#block {@macro [alias=add type=built-in] not an identifier}}{@add 1,2,3}{@add 3,3}");
        test.getProcessor().getRegister().define(builtIn);
        test.results("66");
    }

    @Test
    @DisplayName("Get the built in macro with given alias global")
    void testBuiltInAliasDefinedGlobal() throws Exception {
        final var test = TestThat.theInput(
                "{#block {@macro [alias=:add type=built-in] not an identifier}}{@add 1,2,3}{@add 3,3}");
        test.getProcessor().getRegister().define(builtIn);
        test.results("66");
    }

    @Test
    @DisplayName("Evaluating undefined built-in macro will throw an exception")
    void testBuiltInUndefinedEvaluete() throws Exception {
        TestThat.theInput("{@macro [type=built-in] undefined built-in macro}")
                .throwsBadSyntax("Unknown built-in macro\\{@undefined built-in macro\\}");
    }

    @Test
    @DisplayName("Aliasing undefined built-in macro will return alias undefined")
    void testBuiltInUndefinedAlias() throws Exception {
        TestThat.theInput("{@macro [alias=add builtin]undefined user defined macro}").results("add");
    }

    @Test
    @DisplayName("Evaluating undefined user defined macro will throw an exception")
    void testUserDefinedUndefinedEvaluete() throws Exception {
        TestThat.theInput("{@macro undefined user defined macro}")
                .throwsBadSyntax("Unknown user-defined macro \\{undefined user defined macro\\}");
    }

    @Test
    @DisplayName("Using alias of undefined user defined macro will use default")
    void testUserDefinedUndefinedAliasUsedDefault() throws Exception {
        final var test = TestThat.theInput("{@define default=aaa}{{@macro [alias] undefined user defined macro}}");
        test.getProcessor().getRegister().define(builtIn);
        test.results("aaa");
    }

    @Test
    @DisplayName("Evaluating undefined user defined macro will use default")
    void testUserDefinedUndefinedEvalueteDefault() throws Exception {
        TestThat.theInput("{@define default=aaa}{@macro abrakadabra}").results("aaa");
    }

    @Test
    @DisplayName("Aliasing undefined user defined macro will return alias undefined")
    void testUserDefinedUndefinedAlias() throws Exception {
        TestThat.theInput("{@macro [alias=add]undefined user defined macro}").results("add");
    }

    @Test
    @DisplayName("Throws exception when used on top level with named alias of defined user defined macro")
    void throwsExportException() throws Exception {
        TestThat.theInput("{@define a=}{@macro [alias=add]a}").throwsBadSyntax("Macro 'add' cannot be exported from the top level");
    }

}
