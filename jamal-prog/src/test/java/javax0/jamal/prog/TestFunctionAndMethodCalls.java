package javax0.jamal.prog;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
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

    @DisplayName("Test expressions calling decimals")
    @Test
    void testDecimalCalculations() throws Exception {
        TestThat.theInput("" +
                "{@program\n" +
                " x = decimal(\"sum = 0\")\n" +
                "<< sum:add(\"1.1\") + \"\\n\"\n" +
                "<< sum:add(\"1.1\",\"1.1\") + \"\\n\"\n" +
                "<< sum:mul(\"3.0\") + \"\\n\"\n" +
                "<< sum:div(\"9.9\") + \"\\n\"\n" +
                "<< sum:sub(\"1.1\") + \"\\n\"\n" +
                "<< sum() + \"\\n\"\n" +
                "}{sum}").results(""+
                "1.10\n" +
                "3.30\n" +
                "9.900\n" +
                "1.0\n" +
                "-0.1\n" +
                "-0.1\n" +
                "-0.1"
        );
    }

    @DisplayName("Test infinite division")
    @Test
    void testInifinteDivision() throws Exception {
        TestThat.theInput("" +
                "{@program\n" +
                " x = decimal(\"sum = 10\")\n" +
                " x = sum:div(3)\n"+
                "}{sum}").results(""+
                "3.333333333333333333333333333333333"
        );
    }

}
