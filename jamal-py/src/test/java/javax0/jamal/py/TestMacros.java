package javax0.jamal.py;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestMacros {

    @BeforeAll
    public static void checkPythonAvailability() {
        Assumptions.assumeTrue( new PythonFinder(true).findPythonInterpreter() != null,"Skipping tests: Python is not installed");
    }

    @Test
    @DisplayName("Test a simple Python defined macro")
    void testSimpleMacro() throws BadSyntax, Exception {
        TestThat.theInput("{%@python def myMacro(text):\n" +
                        "    print(f\">>{text}<<\",end='')\n" +
                        "%}" +
                        "{%@myMacro al\"ma%}").usingTheSeparators("{%", "%}")
                .results(">> al\"ma<<");
    }

    @Test
    @DisplayName("Test when the Python code is erronous")
    void testSyntaxErrorMacro() throws BadSyntax, Exception {
        TestThat.theInput("{%@python def myMacro(text):\n" +
                        "    a = a +\n" +
                        "%}" +
                        "{%@myMacro al\"ma%}").usingTheSeparators("{%", "%}")
                .results("name 'myMacro' is not defined");
    }

    @Test
    @DisplayName("Test double defined macro")
    void testSyntaxErrorDoubleDefined() throws BadSyntax, Exception {
        TestThat.theInput("{%@python\n" +
                        "def chubakka(input):\n" +
                        "    print(\"Chubakka says: \"+input,end='')\n" +
                        "%}" +
                        "{%@chubakka broaaaf%}\n" +
                        "{%@python\n" +
                        "def chubakka(input):\n" +
                        "    print(\"Chibakka says: \"+input,end='')\n" +
                        "%}" +
                        "{%@chubakka briaaaf%}").usingTheSeparators("{%", "%}")
                .results("Chubakka says:  broaaaf\nChibakka says:  briaaaf");
    }

    @Test
    @DisplayName("Test a simple Python defined macro with parop defined macro name")
    void testSimpleMacroWithParop() throws BadSyntax, Exception {
        TestThat.theInput("{%@python (id=myMacro)\n" +
                        "#\ndef meMacro(text):\n" +
                        "    print(f\">>{text}<<\",end='')\n" +
                        "myMacro = meMacro\n" +
                        "%}" +
                        "{%@myMacro al\"ma%}").usingTheSeparators("{%", "%}")
                .results(">> al\"ma<<");
    }

    @Test
    @DisplayName("Test a simple Python defined macro with parop defined macro name and function name")
    void testSimpleMacroWithParops() throws BadSyntax, Exception {
        TestThat.theInput("{%@python (id=myMacro function=meMacro)\n" +
                        "#\ndef meMacro(text):\n" +
                        "    print(f\">>{text}<<\",end='')\n" +
                        "%}" +
                        "{%@myMacro al\"ma%}").usingTheSeparators("{%", "%}")
                .results(">> al\"ma<<");
    }
}
