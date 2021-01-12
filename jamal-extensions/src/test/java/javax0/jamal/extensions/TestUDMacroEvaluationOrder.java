package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestUDMacroEvaluationOrder {

    @Test
    void testEvaluationOrder() throws Exception{
        TestThat.theInput("{@define firstName=Julia}{@define k(h)=h, {firstName} h{@define son=Junior Bond}}" +
            "{k /Bond{@define firstName=James}}\n" +
            "{k /Bond}\n"+
            "{son}"
            ).results("Bond, James Bond\n" +
            // TODO: later versions should make the definition of 'firstName' to 'James' local and
            //  result here
            // "Bond, Julia Bond\n"+
            "Bond, James Bond\n"+
            // while keeping this output safe
            "Junior Bond");
    }
}
