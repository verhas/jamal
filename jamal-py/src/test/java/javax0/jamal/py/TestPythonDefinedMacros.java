package javax0.jamal.py;

import javax0.jamal.DocumentConverter;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Optional;
import java.util.Set;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestPythonDefinedMacros {

    @TempDir
    File tempDir;

    @BeforeAll
    public static void checkPythonAvailability() {
        Optional<String> pythonInterpreter = new PythonFinder(true).findPythonInterpreter();
        Assumptions.assumeTrue(pythonInterpreter != null, "Skipping tests: Python is not installed");
        System.out.println("Python interpreter: " + pythonInterpreter.orElseThrow());
    }

    private Path approvalFile;

    @BeforeEach
    void setUp() throws Exception {
        approvalFile = Path.of(tempDir.getAbsolutePath(), ".python.sentinel");
        Files.createFile(approvalFile);
        if (Files.getFileAttributeView(approvalFile, PosixFileAttributeView.class) != null) {
            Files.setPosixFilePermissions(approvalFile, Set.of(PosixFilePermission.OWNER_READ));
        } else {
            Files.getFileAttributeView(approvalFile, DosFileAttributeView.class).setReadOnly(true);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (Files.getFileAttributeView(approvalFile, PosixFileAttributeView.class) != null) {
            Files.setPosixFilePermissions(approvalFile, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } else {
            Files.getFileAttributeView(approvalFile, DosFileAttributeView.class).setReadOnly(false);
        }
        Files.deleteIfExists(approvalFile);
    }

    @Test
    @DisplayName("Test a simple Python defined macro")
    void testSimpleMacro() throws BadSyntax, Exception {
        TestThat.theInput("{%@python def myMacro(text):\n" +
                        "    print(f\">>{text}<<\",end='')\n" +
                        "%}" +
                        "{%@myMacro al\"ma%}").usingTheSeparators("{%", "%}")
                .atPosition(new File(tempDir, "test.txt.jam").getAbsolutePath(), 1, 1)
                .results(">> al\"ma<<");
    }

    @Test
    @DisplayName("Just execute the code, no macro defined")
    void testSimpleExecute() throws BadSyntax, Exception {
        TestThat.theInput("{%@python (execute) def myMacro(text):\n" +
                        "    print(f\">>{text}<<\",end='')\n" +
                        "%}" +
                        "{%@myMacro al\"ma%}").usingTheSeparators("{%", "%}")
                .atPosition(new File(tempDir, "test.txt.jam").getAbsolutePath(), 1, 1)

                .throwsBadSyntax("There is no built.in macro.*");
    }


    @Test
    @DisplayName("Test when the Python code is erronous")
    void testSyntaxErrorMacro() throws BadSyntax, Exception {
        TestThat.theInput("{%@python def myMacro(text):\n" +
                        "    a = a +\n" +
                        "%}").usingTheSeparators("{%", "%}")
                .atPosition(new File(tempDir, "test.txt.jam").getAbsolutePath(), 1, 1)
                .results(s -> s.startsWith("invalid syntax"));
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
                .atPosition(new File(tempDir, "test.txt.jam").getAbsolutePath(), 1, 1)
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
                .atPosition(new File(tempDir, "test.txt.jam").getAbsolutePath(), 1, 1)
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
                .atPosition(new File(tempDir, "test.txt.jam").getAbsolutePath(), 1, 1)
                .results(">> al\"ma<<");
    }

    @Test
    @DisplayName("Error while executing the macro")
    void errorWhileMacro() throws BadSyntax, Exception {
        TestThat.theInput("{%@python \n" +
                        "def meMacro(text):\n" +
                        "    print(f\">>{text/0}<<\",end='')\n" +
                        "%}" +
                        "{%@meMacro 3%}").usingTheSeparators("{%", "%}")
                .atPosition(new File(tempDir, "test.txt.jam").getAbsolutePath(), 1, 1)
                .results("unsupported operand type(s) for /: 'str' and 'int'");
    }

    @Test
    @DisplayName("Test it is not in a venv")
    void testNotVenv() throws BadSyntax, Exception {
        TestThat.theInput("{%@python (execute)\n" +
                        "import sys\n" +
                        "\n" +
                        "if hasattr(sys, 'real_prefix') or (hasattr(sys, 'base_prefix') and sys.base_prefix != sys.prefix):\n" +
                        "    print(\"Running inside a virtual environment.\", end='')\n" +
                        "else:\n" +
                        "    print(\"Not running in a virtual environment.\", end='')%}"
                ).usingTheSeparators("{%", "%}")
                .atPosition(new File(tempDir, "test.txt.jam").getAbsolutePath(), 1, 1)
                .results("Not running in a virtual environment.");
    }

    // @Test it will go into integration test executing in docker
    @DisplayName("Test it is a venv")
    void tesVenv() throws BadSyntax, Exception {
        Assumptions.assumeTrue(new File(DocumentConverter.getRoot() + "/jamal-py/venv").exists());
        TestThat.theInput("{%#python (execute directory=\"{%@dev:root%}/jamal-py\")\n" +
                        "import sys\n" +
                        "\n" +
                        "if hasattr(sys, 'real_prefix') or (hasattr(sys, 'base_prefix') and sys.base_prefix != sys.prefix):\n" +
                        "    print(\"Running inside a virtual environment.\", end='')\n" +
                        "else:\n" +
                        "    print(\"Not running in a virtual environment.\", end='')%}"
                ).usingTheSeparators("{%", "%}")
                .results("Running inside a virtual environment.");
    }

    @Test
    @DisplayName("Test self closer")
    void tesCloserDefined() throws BadSyntax, Exception {
        final var file = Paths.get("test.txt").toFile();
        final var fileName = file.getAbsolutePath().replace("\\", "/");
        ;
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        file.deleteOnExit();
        TestThat.theInput("{%#python (close)\n" +
                        "with open(\"" + fileName + "\",\"w\") as file:\n" +
                        "    file.write(\"hello\")\n" +
                        "%}"
                ).usingTheSeparators("{%", "%}")
                .atPosition(new File(tempDir, "test.txt.jam").getAbsolutePath(), 1, 1)
                .results("");
        Assertions.assertTrue(file.exists(), "File " + file.getAbsolutePath() + " does not exist");
    }
}
