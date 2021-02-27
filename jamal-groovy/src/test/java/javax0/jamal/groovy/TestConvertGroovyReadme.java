package javax0.jamal.groovy;

import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import org.junit.jupiter.api.Test;

public class TestConvertGroovyReadme {
    private static void generateAdoc(String directory) throws Exception {
        generateAdoc(directory, "README");
    }

    private static void generateAdoc(final String directory, final String fileName) throws Exception {
        generateDoc(directory, fileName, "adoc");
    }

    private static void generateDoc(final String directory, final String fileName, final String ext) throws Exception {
        final var in = FileTools.getInput(directory + "/" + fileName + "." + ext + ".jam");
        // snippet Groovy_Jamal_Doc_Execution
        final var processor = new Processor("{%", "%}");
        final var shell = Shell.getShell(processor,Shell.DEFAULT_GROOVY_SHELL_NAME);
        shell.property("processor",new Processor("{", "}"));
        processor.defineGlobal(shell);
        final var result = processor.process(in);
        // end snippet
        FileTools.writeFileContent(directory + "/" + fileName + "." + ext, result);
    }


    @Test
    void convertGroovyReadme() throws Exception {
        generateAdoc("../jamal-groovy");
    }
}
