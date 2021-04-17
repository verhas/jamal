package javax0.jamal.yaml;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestDefine {

    @Test
    @DisplayName("Test that a yaml object can be defined and then used as a user defined macro")
    void testYamlUserDefinedMacroCreation() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define z=\n" +
            "a: 1\n" +
            "b: 2" +
            "}{@verbatim z}"
        ).results("{a: 1, b: 2}\n");

        TestThat.theInput("" +
            "{@yaml:define z=\n" +
            "- a: 1\n" +
            "  b: 2" +
            "}{@verbatim z}"
        ).results("- {a: 1, b: 2}\n");
    }
}
