package javax0.jamal.documentation;

import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import org.junit.jupiter.api.Test;

public class TestConvertReadme {

    private static void generateReadmeAdoc(String directory) throws Exception {
        final var in = FileTools.getInput(directory + "/README.adoc.jam");
        final var processor = new Processor("{%", "%}");
        final var result = processor.process(in);
        FileTools.writeFileContent(directory + "/README.adoc", result);
    }

    @Test
    void convertTopReadme() throws Exception {
        generateReadmeAdoc("..");
    }

    @Test
    void convertExtensionReadme() throws Exception {
        generateReadmeAdoc("../jamal-extensions");
    }

    @Test
    void convertScriptBasicReadme() throws Exception {
        generateReadmeAdoc("../jamal-scriptbasic");
    }

    @Test
    void convertSnippetReadme() throws Exception {
        generateReadmeAdoc(".");
    }
}
