package javax0.jamal.py;

import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestPythonFinder {


    @BeforeAll
    public static void checkPythonAvailability() {
        PythonFinder.interpreter.set(null);
        Assumptions.assumeTrue(new PythonFinder(true).findPythonInterpreter() != null, "Skipping tests: Python is not installed");
        Assumptions.assumeTrue(System.getenv(PythonFinder.ENV_JAMAL_PYTHON_INTERPRETER) == null, "Skipping tests: Python is configured in environment");
    }

    @BeforeEach
    public void reset() {
        PythonFinder.interpreter.set(null);
    }

    @Test
    @DisplayName("Find the python interpreter")
    void findPythonInterpreter() {
        System.clearProperty("jamal.python.interpreter");
        final var interpreter = new PythonFinder(false).findPythonInterpreter();
        Assertions.assertNotNull(interpreter);
    }

    @Test
    @DisplayName("Find the configured python interpreter")
    void findPythonInterpreterConfiguredOK() {
        final var interpreter = new PythonFinder(false).findPythonInterpreter().orElseThrow();
        System.setProperty("jamal.python.interpreter", interpreter);
        PythonFinder.interpreter.set(null);
        final var configured = new PythonFinder(true).findPythonInterpreter().orElseThrow();
        Assertions.assertEquals(interpreter, configured);
        System.clearProperty("jamal.python.interpreter");
    }

    @Test
    @DisplayName("Configured python interpreter does not exist")
    void findPythonInterpreterConfiguredBad() {
        System.setProperty("jamal.python.interpreter", "something nonsense");
        final var configured = new PythonFinder(true).findPythonInterpreter();
        System.clearProperty("jamal.python.interpreter");
        Assertions.assertTrue(configured.isEmpty());
    }

    @Test
    @DisplayName("Configured python interpreter does not exist, but no check")
    void findPythonInterpreterConfiguredBadNoCheck() {
        System.setProperty("jamal.python.interpreter", "something nonsense");
        final var configured = new PythonFinder(false).findPythonInterpreter().orElseThrow();
        System.clearProperty("jamal.python.interpreter");
        Assertions.assertEquals("something nonsense", configured);
    }
}
