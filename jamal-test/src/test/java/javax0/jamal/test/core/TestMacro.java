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

        @Override
        public int expectedNumberOfArguments() {
            return -1;
        }
    };

    final Macro macro = new Macro() {
        @Override
        public String evaluate(final Input in, final Processor processor) {
            if( in.length() == 0 ) {
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
    void testUdAliasDefined() throws Exception {
        final var test = TestThat.theInput("{{@macro [alias=add] +++}/1/2/3}{add/1/2/3}{add/3/3}");
        test.getProcessor().getRegister().define(ud);
        test.results("666");
    }

    @Test
    @DisplayName("Get the built in macro name")
    void testBuiltInName() throws Exception {

        final var test = TestThat.theInput("{@macro [type=builtin]not an identifier}");
        test.getProcessor().getRegister().define(macro);
        test.results("no param");
    }

    @Test
    @DisplayName("Get the built in macro with alias")
    void testBuiltInAlias() throws Exception {
        final var test = TestThat.theInput("{#{@macro [alias type=built-in] not an identifier} 1,2,3}");
        test.getProcessor().getRegister().define(macro);
        test.results("6");
    }

    @Test
    @DisplayName("Get the built in macro with given alias")
    void testBuiltInAliasDefined() throws Exception {
        final var test = TestThat.theInput("{#{@macro [alias=add type=\"built in\"] not an identifier} 1,2,3}{@add 1,2,3}{@add 3,3}");
        test.getProcessor().getRegister().define(macro);
        test.results("666");
    }
}
