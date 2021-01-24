package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestUserDefinedPostEval {

    @Test
    void testPostEval() throws Exception {
        TestThat.theInput("{@define a=this is it}\\\n" +
            "{@define b={`a}}\\\n" +
            "{@define c={`b}}\\\n" +
            "{@define userDefined={`c}}\\\n" +
            "{userDefined}\n" +
            "{!userDefined}\n" +
            "{!!userDefined}\n" +
            "{!!!userDefined}").results("{c}\n" +
            "{b}\n" +
            "{a}\n" +
            "this is it");
    }
}
