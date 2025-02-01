package javax0.jamal.py;

import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestPythonFinder {


    @BeforeAll
    public static void checkPythonAvailability() {
        PythonFinder.interpreter.set(null);
        Assumptions.assumeTrue(new PythonFinder(true).findPythonInterpreter() != null, "Skipping tests: Python is not installed");
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
        final var interpreter = new PythonFinder(false).findPythonInterpreter();
        System.setProperty("jamal.python.interpreter", interpreter);
        final var configured = new PythonFinder(true).findPythonInterpreter();
        Assertions.assertEquals(interpreter, configured);
        System.clearProperty("jamal.python.interpreter");
    }

    @Test
    @DisplayName("Configured python interpreter does not exist")
    void findPythonInterpreterConfiguredBad() {
        System.setProperty("jamal.python.interpreter", "something nonsense");
        final var configured = new PythonFinder(true).findPythonInterpreter();
        System.clearProperty("jamal.python.interpreter");
        Assertions.assertNull(configured);
    }

    @Test
    @DisplayName("Configured python interpreter does not exist, but no check")
    void findPythonInterpreterConfiguredBadNoCheck() {
        System.setProperty("jamal.python.interpreter", "something nonsense");
        final var configured = new PythonFinder(false).findPythonInterpreter();
        System.clearProperty("jamal.python.interpreter");
        Assertions.assertEquals("something nonsense", configured);
    }
}
