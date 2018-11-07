package javax0.jamal.extensions;

import javax0.jamal.test.TestMacro;
import org.junit.jupiter.api.Test;

public class TestCase {

    @Test
    public void testCaseLower() throws Exception {
        var tester = TestMacro.forMacro(Case.Lower.class);
        tester.test("INPUT", "input");
        tester.test("INpUT", "input");
        tester.test("INpuT", "input");
        tester.test("INput", "input");
        tester.test("Input", "input");
        tester.test("input", "input");
    }
    @Test
    public void testCaseUpper() throws Exception {
        var tester = TestMacro.forMacro(Case.Upper.class);
        tester.test("INPUT", "INPUT");
        tester.test("INpUT", "INPUT");
        tester.test("INpuT", "INPUT");
        tester.test("INput", "INPUT");
        tester.test("Input", "INPUT");
        tester.test("input", "INPUT");
    }
    @Test
    public void testCaseCap() throws Exception {
        var tester = TestMacro.forMacro(Case.Cap.class);
        tester.test("INPUT", "INPUT");
        tester.test("INpUT", "INpUT");
        tester.test("INpuT", "INpuT");
        tester.test("INput", "INput");
        tester.test("Input", "Input");
        tester.test("input", "Input");
    }
    @Test
    public void testCaseDecap() throws Exception {
        var tester = TestMacro.forMacro(Case.Decap.class);
        tester.test("INPUT", "iNPUT");
        tester.test("INpUT", "iNpUT");
        tester.test("INpuT", "iNpuT");
        tester.test("INput", "iNput");
        tester.test("Input", "input");
        tester.test("input", "input");
    }
}
