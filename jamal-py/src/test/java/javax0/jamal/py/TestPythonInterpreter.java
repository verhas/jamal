package javax0.jamal.py;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPythonInterpreter {

    @Test
    void testPython() throws Exception {
        final String hello;
        try (final var python = new PythonInterpreter(null,null)) {
            hello = python.execute("" +
                    "def a():\n" +
                    "    print(\"hello\", end='')\n" +
                    "a()"
            );
        }
        Assertions.assertEquals("hello", hello);
    }
}
