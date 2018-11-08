package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestCase {

    @Test
    public void testCaseLower() throws Exception {
        var lowerCase = TestThat.forMacro(Case.Lower.class);
        lowerCase.fromInput("INPUT").results("input");
        lowerCase.fromInput("INpUT").results("input");
        lowerCase.fromInput("INpuT").results("input");
        lowerCase.fromInput("INput").results("input");
        lowerCase.fromInput("Input").results("input");
        lowerCase.fromInput("input").results("input");
    }
    @Test
    public void testCaseUpper() throws Exception {
        var upperCase = TestThat.forMacro(Case.Upper.class);
        upperCase.fromInput("INPUT").results("INPUT");
        upperCase.fromInput("INpUT").results("INPUT");
        upperCase.fromInput("INpuT").results("INPUT");
        upperCase.fromInput("INput").results("INPUT");
        upperCase.fromInput("Input").results("INPUT");
        upperCase.fromInput("input").results("INPUT");
    }
    @Test
    public void testCaseCap() throws Exception {
        var capitalize = TestThat.forMacro(Case.Cap.class);
        capitalize.fromInput("INPUT").results("INPUT");
        capitalize.fromInput("INpUT").results("INpUT");
        capitalize.fromInput("INpuT").results("INpuT");
        capitalize.fromInput("INput").results("INput");
        capitalize.fromInput("Input").results("Input");
        capitalize.fromInput("input").results("Input");
    }
    @Test
    public void testCaseDecap() throws Exception {
        var decapitalize = TestThat.forMacro(Case.Decap.class);
        decapitalize.fromInput("INPUT").results("iNPUT");
        decapitalize.fromInput("INpUT").results("iNpUT");
        decapitalize.fromInput("INpuT").results("iNpuT");
        decapitalize.fromInput("INput").results("iNput");
        decapitalize.fromInput("Input").results("input");
        decapitalize.fromInput("input").results("input");
    }
}
