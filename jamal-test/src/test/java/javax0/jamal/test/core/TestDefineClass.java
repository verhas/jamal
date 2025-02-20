package javax0.jamal.test.core;

import javax0.jamal.api.Configurable;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestDefineClass {

    /**
     * This is a class that is a user-defined macro. It is used here to test that the 'define' macro can really define
     * a user-defined macro, which is given by a class name.
     */
    public static class TestUserDefinedMacro implements Identified, Evaluable, Configurable {
        private String id;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String evaluate(String... parameters) {
            return "test";
        }

        @Override
        public void configure(Keys key, Object value) {
            switch (key) {
                case ID:
                    id = (String) value;
                    break;
                case VERBATIM:
                    break;
                case TAIL:
                    break;
                case PARAMS:
                    break;
                case PROCESSOR:
                    break;
                case INPUT:
                    break;
                default:
                    throw new RuntimeException("Unknown key: " + key);
            }
        }
    }

    @Test
    @DisplayName("Test that a user defined macro can be implemented in Java and defined using the class name")
    void testJavaDefinedUserDefinedMacro() throws Exception {
        TestThat.theInput("{@define [class=javax0.jamal.test.core.TestDefineClass$TestUserDefinedMacro]test=test}{test}").results("test");
    }

}
