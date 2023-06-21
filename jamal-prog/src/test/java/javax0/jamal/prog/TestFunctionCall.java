package javax0.jamal.prog;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestFunctionCall {

    @Test
    void testCall() throws Exception {
        TestThat.theInput("" +
                "{@define f(A,B)=ABBA}" +
                "{@define S=ABBA}" +
                "{@do \n" +
                "<< string:contains(\"text=abraba\",\"abraba habra\")\n" +
                "<< define(\"verbatim\",\"f(A,B)=ABBA\")\n" +
                "<< f(23,\"{S}\")\n" +
                "<< define(\"f(A,B)=ABBA\")\n" +
                "<< !f(23,\"{S}\")\n" +
                "<< f(23,45+1)\n" +
                "}").results("true23{S}{S}2323ABBAABBA2323464623");
    }

}
