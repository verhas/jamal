package javax0.jamal.test.core;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.testsupport.TestAll;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestSamples {
    private javax0.jamal.api.Input createInput(String testFile) throws IOException {
        var fileName = this.getClass().getResource(testFile).getFile();
        fileName = fixupPath(fileName);
        var fileContent = Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"));
        return new Input(fileContent, new Position(fileName));
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

    @DisplayName("Test all files that have an '.expected' pair")
    @Test
    void testExpectedFiles() throws IOException, BadSyntax {
        TestAll.testExpected(this,Assertions::assertEquals);
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
