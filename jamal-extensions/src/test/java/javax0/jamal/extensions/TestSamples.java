package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestSamples {
    private javax0.jamal.api.Input createInput(String testFile) throws IOException {
        var fileName = this.getClass().getResource(testFile).getFile();
        fileName = fixupPath(fileName);
        var fileContent = Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"));
        return new Input(new StringBuilder(fileContent), fileName);
    }

    /**
     * Fixup the JDK bug JDK-8197918
     *
     * @param fileName the file name that may contain an erroneous leading / on Windows
     * @return the fileName without the leading / if it contains ':', so it is assumed this is Windows
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

    @Test
    @DisplayName("for loop iterates through the elements")
    void testForLoop() throws IOException, BadSyntax {
        assertEquals(" a is either a, b, c or d\n" +
                " b is either a, b, c or d\n" +
                " c is either a, b, c or d\n" +
                " d is either a, b, c or d\n" +
                " a is either a, b, c or d\n" +
                " b is either a, b, c or d\n" +
                " c is either a, b, c or d\n" +
                " d is either a, b, c or d\n", result("for.jam"));
    }
}
