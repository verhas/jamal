package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.testsupport.TestAll;
import javax0.jamal.testsupport.TestThat;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestSamples {
    private javax0.jamal.api.Input createInput(String testFile) throws IOException {
        var fileName = this.getClass().getResource(testFile).getFile();
        fileName = fixupPath(fileName);
        try (final var lines = Files.lines(Paths.get(fileName))) {
            var fileContent = lines.collect(Collectors.joining("\n"));
            return new Input(fileContent, new Position(fileName));
        }
    }

    /**
     * Fixup the JDK bug JDK-8197918
     *
     * @param fileName the file name that may contain an erroneous leading / on Windows
     * @return the file without the leading / if it contains ':', so it is assumed this is Windows
     */
    private String fixupPath(String fileName) {
        if (fileName.contains(":")) {
            fileName = fileName.substring(1);
        }
        return fileName;
    }

    private String result(String testFile) throws IOException, BadSyntax {
        var in = createInput(testFile);
        final var sut = new Processor("{", "}");
        return sut.process(in);
    }

    @Test
    @DisplayName("simple import")
    void testSimpleImport() throws IOException, BadSyntax {
        assertEquals("thisIsGood\n" +
                "ThisIsAlsoGood\n" +
                "THIS_IS_EXTRA_GOOD\n" +
                "this is extra good\n" +
                "This is extra good.", result("use.jam"));
    }


    @DisplayName("Test all files that have an '.expected' pair")
    @Test
    void testExpectedFiles() throws IOException, BadSyntax {
        TestAll.testExpected(this, Assertions::assertEquals);
    }

    @DisplayName("Test all files that have an '.err' extension")
    @Test
    void testErrFiles() throws IOException, BadSyntax {
        final TestAll tests = TestAll.in(this.getClass()).filesWithExtension(".err");
        if (!tests.failAsExpected()) {
            Assertions.assertEquals(tests.getExpected(), tests.getActual(), tests.getMessage());
        }
    }
}
