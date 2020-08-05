package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestCase {

    @Test
    public void testCaseLower() throws Exception {
        var lowerCase = TestThat.theMacro(Case.Lower.class);
        lowerCase.fromTheInput("INPUT").results("input");
        lowerCase.fromTheInput("INpUT").results("input");
        lowerCase.fromTheInput("INpuT").results("input");
        lowerCase.fromTheInput("INput").results("input");
        lowerCase.fromTheInput("Input").results("input");
        lowerCase.fromTheInput("input").results("input");
    }

    @Test
    public void testCaseUpper() throws Exception {
        var upperCase = TestThat.theMacro(Case.Upper.class);
        upperCase.fromTheInput("INPUT").results("INPUT");
        upperCase.fromTheInput("INpUT").results("INPUT");
        upperCase.fromTheInput("INpuT").results("INPUT");
        upperCase.fromTheInput("INput").results("INPUT");
        upperCase.fromTheInput("Input").results("INPUT");
        upperCase.fromTheInput("input").results("INPUT");
    }

    @Test
    public void testCaseCap() throws Exception {
        var capitalize = TestThat.theMacro(Case.Cap.class);
        capitalize.fromTheInput("INPUT").results("INPUT");
        capitalize.fromTheInput("INpUT").results("INpUT");
        capitalize.fromTheInput("INpuT").results("INpuT");
        capitalize.fromTheInput("INput").results("INput");
        capitalize.fromTheInput("Input").results("Input");
        capitalize.fromTheInput("input").results("Input");
    }

    @Test
    public void testCaseDecap() throws Exception {
        var decapitalize = TestThat.theMacro(Case.Decap.class);
        decapitalize.fromTheInput("INPUT").results("iNPUT");
        decapitalize.fromTheInput("INpUT").results("iNpUT");
        decapitalize.fromTheInput("INpuT").results("iNpuT");
        decapitalize.fromTheInput("INput").results("iNput");
        decapitalize.fromTheInput("Input").results("input");
        decapitalize.fromTheInput("input").results("input");
    }
}
