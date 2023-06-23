package javax0.jamal.test.core;

import javax0.jamal.api.Configurable;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestDefineClass {

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
        public void configure(String key, Object value) {
            switch (key) {
                case "id":
                    id = (String) value;
                    break;
                case "verbatim":
                    break;
                case "tail":
                    break;
                case "params":
                    break;
                default:
                    throw new RuntimeException("Unknown key: " + key);
            }
        }
    }

    @Test
    @DisplayName("Test that a user defined macro can be implemented in Java and defined using the class name")
    void testJavaDefinedUserDefinedMacro() throws Exception {
        TestThat.theInput("{@define [class]test=javax0.jamal.test.core.TestDefineClass$TestUserDefinedMacro}{test}").results("test");
    }

}
