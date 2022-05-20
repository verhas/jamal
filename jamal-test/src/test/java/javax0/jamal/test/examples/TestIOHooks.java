package javax0.jamal.test.examples;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.InputHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestIOHooks {

    private static final class MockIO implements javax0.jamal.api.Processor.FileReader, javax0.jamal.api.Processor.FileWriter {
        private static final Map<String, String> mockFileSystem =
                new HashMap<>(
                Map.of(
                        "f0", "file 1",
                        "f2", "{@include f1}",
                        "dd/f3", "{@include ../f2}"
                ));

        @Override
        public javax0.jamal.api.Processor.IOHookResult read(final String fileName) {
            if (fileName.equals("f1")) {
                return new javax0.jamal.api.Processor.IOHookResultRedirect("f0");
            }
            return new javax0.jamal.api.Processor.IOHookResultDone(mockFileSystem.get(fileName));
        }

        @Override
        public javax0.jamal.api.Processor.IOHookResult write(final String fileName, final String content) {
            if (fileName.equals("yayy")) {
                return new javax0.jamal.api.Processor.IOHookResultRedirect("SNAFU");
            }
            mockFileSystem.put(fileName,content);
            return new javax0.jamal.api.Processor.IOHookResultDone();
        }

        String get(final String fn){
            return mockFileSystem.get(fn);
        }
    }

    public static final class Write implements Macro {

        @Override
        public String evaluate(final Input in, final javax0.jamal.api.Processor processor) throws BadSyntax {
            final var parts = InputHandler.getParts(in,2);
            FileTools.writeFileContent(parts[0], parts[1], processor);
            return "";
        }
    }

    @Test
    public void testIOHooks() throws Exception {
        final var processor = new Processor();
        final var mockIO = new MockIO();
        processor.setFileReader(mockIO);
        processor.setFileWriter(mockIO);
        final var s = processor.process("{@use javax0.jamal.test.examples.TestIOHooks.Write}{@include dd/f3}{@write/yayy/this is what we will write}");
        Assertions.assertEquals("file 1", s);
        Assertions.assertEquals("this is what we will write",mockIO.get("SNAFU"));
    }
}
