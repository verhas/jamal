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
             "Bond, Julia Bond\n"+
            "Junior Bond");
    }

    @Test
    void testBug()throws Exception{
        TestThat.theInput("{@define a=this is it}{@define b={a}}{#define c={@verbatim b}}{c} {@verbatim c}")
            .results("this is it {a}");
    }

}
