package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestCamel {

    @Test
    // snippet TestCamel
    void testCamelLowerCase() throws Exception {
        var camelLowerCase = TestThat.theMacro(Camel.LowCamel.class);
        camelLowerCase.fromTheInput("INPUT").results("input");
        camelLowerCase.fromTheInput("INpUT").results("input");
        camelLowerCase.fromTheInput("INpuT").results("input");
        camelLowerCase.fromTheInput("INput").results("input");
        camelLowerCase.fromTheInput("Input").results("input");
        camelLowerCase.fromTheInput("input").results("input");
        camelLowerCase.fromTheInput("IN-PUT").results("inPut");
        camelLowerCase.fromTheInput("I-N-P-U-T").results("iNPUT");
    }
    // end snippet

    @Test
    void testCamelUpperCase() throws Exception {
        var camelUpperCase = TestThat.theMacro(Camel.UppCamel.class);
        camelUpperCase.fromTheInput("INPUT").results("Input");
        camelUpperCase.fromTheInput("INpUT").results("Input");
        camelUpperCase.fromTheInput("INpuT").results("Input");
        camelUpperCase.fromTheInput("INput").results("Input");
        camelUpperCase.fromTheInput("Input").results("Input");
        camelUpperCase.fromTheInput("input").results("Input");
        camelUpperCase.fromTheInput("IN-PUT").results("InPut");
        camelUpperCase.fromTheInput("I-N-P-U-T").results("INPUT");
    }

    @Test
    void testCamelCStyle() throws Exception {
        var cstyle = TestThat.theMacro(Camel.CStyle.class);
        cstyle.fromTheInput("_Input").results("INPUT");
        cstyle.fromTheInput("_input").results("INPUT");
        cstyle.fromTheInput("_InPut").results("IN_PUT");
        cstyle.fromTheInput("_inPut").results("IN_PUT");
        cstyle.fromTheInput("_InPuT").results("IN_PU_T");
        cstyle.fromTheInput("_inPuT").results("IN_PU_T");
    }

    @Test
    void testCamelSentence() throws Exception {
        var sentence = TestThat.theMacro(Camel.Sentence.class);
        sentence.fromTheInput("Input").results("input");
        sentence.fromTheInput("input").results("input");
        sentence.fromTheInput("InPut").results("in put");
        sentence.fromTheInput("inPut").results("in put");
        sentence.fromTheInput("InPuT").results("in pu t");
        sentence.fromTheInput("inPuT").results("in pu t");
    }
}
