package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestTry {

    @Test
    void testTry() throws Exception {
        TestThat.theInput("{@try {undefinedMacro}}").results("");
        TestThat.theInput("{@try! {undefinedMacro}}").results("User defined macro '{undefinedMacro ...' is not defined.");
        TestThat.theInput("{@try! just blabla}").results("just blabla");
        TestThat.theInput("{@try? {undefinedMacro}}").results("false");
        TestThat.theInput("{@define ~ x={undefinedMacro}}{#try? {x}}").results("false");
    }

}
