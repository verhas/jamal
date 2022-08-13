package javax0.jamal.debugger;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

/**
 * Deliberately named so that the surefire debugger does not start it.
 */
public class ManualTestForDebugMacro {
    @Test
    void test1() throws Exception {
        TestThat.theInput("{@define a=1}{@debug using=\"http:8080\"}{a}{a}{@debug off}").results();
    }

    @Test
    void tes2() throws Exception {
        System.setProperty("jamal.debug", "http:8080");
        TestThat.theInput("{@define a=1}{a}").results();
    }
}
