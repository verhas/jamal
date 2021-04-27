package javax0.jamal.documentation;

import javax0.jamal.DocumentConverter;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import org.junit.jupiter.api.Test;

public class TestConvertReadme {

    private static void generateAdoc(String directory) throws Exception {
        generateAdoc(directory, "README");
    }

    private static void generateAdoc(final String directory, final String fileName) throws Exception {
        generateAdoc(directory, fileName, "adoc");
    }

    private static void generateAdoc(final String directory, final String fileName, final String ext) throws Exception {
        final var in = FileTools.getInput(directory + "/" + fileName + "." + ext + ".jam");
        final var processor = new Processor("{%", "%}");
        final var result = processor.process(in);
        FileTools.writeFileContent(directory + "/" + fileName + "." + ext, result);
    }

    @Test
    void convertPlantUMLReadme() throws Exception {
        DocumentConverter.convert("./README.adoc.jam");
    }
}
