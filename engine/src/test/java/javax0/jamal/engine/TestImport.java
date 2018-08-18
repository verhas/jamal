package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImport {


    private javax0.jamal.api.Input createInput(String testFile) throws IOException {
        var fileName = this.getClass().getResource(testFile).getFile();
        var fileContent = Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"));
        return new Input(new StringBuilder(fileContent), fileName);
    }

    private String result(String testFile) throws IOException, BadSyntax {
        var in = createInput(testFile);
        final var sut = new Processor("{", "}");
        return sut.process(in);
    }

    @Test
    @DisplayName("simple import")
    public void testSimpleImport() throws IOException, BadSyntax {
        assertEquals("almakorte", result("test_import.jam"));
    }

    @Test
    @DisplayName("import definitions")
    public void testImportDefine() throws IOException, BadSyntax {
        assertEquals("xxx is defined already", result("import_defines.jam"));
    }
}
