package javax0.jamal.extensions;

import javax0.jamal.test.TestMacro;
import org.junit.jupiter.api.Test;

public class TestCamel {

    @Test
    public void testCamelLowerCase() throws Exception {
        var tester = TestMacro.forMacro(Camel.LowerCase.class);
        tester.test("INPUT", "input");
        tester.test("INpUT", "input");
        tester.test("INpuT", "input");
        tester.test("INput", "input");
        tester.test("Input", "input");
        tester.test("input", "input");
    }

    @Test
    public void testCamelUpperCase() throws Exception {
        var tester = TestMacro.forMacro(Camel.UpperCase.class);
        tester.test("INPUT", "Input");
        tester.test("INpUT", "Input");
        tester.test("INpuT", "Input");
        tester.test("INput", "Input");
        tester.test("Input", "Input");
        tester.test("input", "Input");
    }

    @Test
    public void testCamelCStyle() throws Exception {
        var tester = TestMacro.forMacro(Camel.CStyle.class);
        tester.test("_Input", "INPUT");
        tester.test("_input", "INPUT");
        tester.test("_InPut", "IN_PUT");
        tester.test("_inPut", "IN_PUT");
        tester.test("_InPuT", "IN_PU_T");
        tester.test("_inPuT", "IN_PU_T");
    }
    @Test
    public void testCamelSentence() throws Exception {
        var tester = TestMacro.forMacro(Camel.Sentence.class);
        tester.test("Input", "input");
        tester.test("input", "input");
        tester.test("InPut", "in put");
        tester.test("inPut", "in put");
        tester.test("InPuT", "in pu t");
        tester.test("inPuT", "in pu t");
    }
}
