package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestCamel {

    @Test
    public void testCamelLowerCase() throws Exception {
        var camelLowerCase = TestThat.forMacro(Camel.LowerCase.class);
        camelLowerCase.fromInput("INPUT").results( "input");
        camelLowerCase.fromInput("INpUT").results( "input");
        camelLowerCase.fromInput("INpuT").results( "input");
        camelLowerCase.fromInput("INput").results( "input");
        camelLowerCase.fromInput("Input").results( "input");
        camelLowerCase.fromInput("input").results( "input");
        camelLowerCase.fromInput("IN-PUT").results( "inPut");
        camelLowerCase.fromInput("I-N-P-U-T").results( "iNPUT");
    }

    @Test
    public void testCamelUpperCase() throws Exception {
        var camelUpperCase = TestThat.forMacro(Camel.UpperCase.class);
        camelUpperCase.fromInput("INPUT").results( "Input");
        camelUpperCase.fromInput("INpUT").results( "Input");
        camelUpperCase.fromInput("INpuT").results( "Input");
        camelUpperCase.fromInput("INput").results( "Input");
        camelUpperCase.fromInput("Input").results( "Input");
        camelUpperCase.fromInput("input").results( "Input");
        camelUpperCase.fromInput("IN-PUT").results( "InPut");
        camelUpperCase.fromInput("I-N-P-U-T").results( "INPUT");
    }

    @Test
    public void testCamelCStyle() throws Exception {
        var cstyle = TestThat.forMacro(Camel.CStyle.class);
        cstyle.fromInput("_Input").results( "INPUT");
        cstyle.fromInput("_input").results( "INPUT");
        cstyle.fromInput("_InPut").results( "IN_PUT");
        cstyle.fromInput("_inPut").results( "IN_PUT");
        cstyle.fromInput("_InPuT").results( "IN_PU_T");
        cstyle.fromInput("_inPuT").results( "IN_PU_T");
    }
    @Test
    public void testCamelSentence() throws Exception {
        var sentence = TestThat.forMacro(Camel.Sentence.class);
        sentence.fromInput("Input").results( "input");
        sentence.fromInput("input").results( "input");
        sentence.fromInput("InPut").results( "in put");
        sentence.fromInput("inPut").results( "in put");
        sentence.fromInput("InPuT").results( "in pu t");
        sentence.fromInput("inPuT").results( "in pu t");
    }
}
