package javax0.jamal.prog;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestFunctionAndMethodCalls {

    @Test
    void testFunctionCall() throws Exception {
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

    @Test
    void testFunctionCallFail() throws Exception {
        TestThat.theInput("" +
                "{@do \n" +
                "<< f(23,45+1)\n" +
                "}").throwsBadSyntax("Macro for the function 'f\\(\\)' is not defined\\.");
    }

    @Test
    void testMethodCall() throws Exception {
        TestThat.theInput("" +
                "{@define f(A,B)=ABBA}" +
                "{@define S=ABBA}" +
                "{@do \n" +
                "<< define(\"f(A,B)=BA\")\n" +
                "<< (\"/zebra/\"+f(\"RA\"\"ZEB\")).string:equals(\"ignoreCase\")\n" +
                "}").results("true");
    }

    @Test
    void testMethodChainedCall() throws Exception {
        TestThat.theInput("" +
                "{@program\n" +
                "<< \"this is a string\".string:substring(\"begin=1\",\"end=5\").case:upper()\n" +
                "}").results("HIS");
    }


}
