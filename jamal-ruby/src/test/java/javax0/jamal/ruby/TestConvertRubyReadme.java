package javax0.jamal.ruby;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;
import javax0.jamal.testsupport.SentinelSmith;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Test;

public class TestConvertRubyReadme {
    private static void generateAdoc(String directory) throws Exception {
        generateAdoc(directory, "README");
    }

    private static void generateAdoc(final String directory, final String fileName) throws Exception {
        generateDoc(directory, fileName, "adoc");
    }

    // snippet MyProcessor
    public static class MyProcessor {
        final Processor processor = new Processor("{", "}");

        public String process(String s) throws BadSyntax {
            return processor.process(Input.makeInput(s));
        }
    }
    // end snippet

    private static void generateDoc(final String directory, final String fileName, final String ext) throws Exception {
        // snippet Ruby_Jamal_Doc_Execution
        final var processor = new Processor("{%", "%}");
        final var in = FileTools.getInput(directory + "/" + fileName + "." + ext + ".jam", processor);
        final var shell = Shell.getShell(processor, Shell.DEFAULT_RUBY_SHELL_NAME);
        shell.property("$processor", new MyProcessor());
        processor.defineGlobal(shell);
        final var result = processor.process(in);
        // end snippet
        FileTools.writeFileContent(directory + "/" + fileName + "." + ext, result, processor);
    }


    @Test
    void convertRubyReadme() throws Exception {
        SentinelSmith.forge("ruby");
        generateAdoc("../jamal-ruby");
    }
}
