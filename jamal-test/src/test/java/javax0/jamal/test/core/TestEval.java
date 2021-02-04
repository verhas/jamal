package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestEval {

    @Test
    void testJShellEval()throws Exception{
        TestThat.theInput(
            "{@eval/JShell 1+3}\n" +
                "{@define a=1}{@define b=2}\\\n" +
                "{#eval/JShell {a}+{b}}"
        ).results(
            "4\n" +
                "3"
        );
    }

    @Test
    void testDelayedEval()throws Exception{
        TestThat.theInput(
            "{@define a=2}{a}{@eval {a}}"
        ).results(
                "22"
        );
    }
}
