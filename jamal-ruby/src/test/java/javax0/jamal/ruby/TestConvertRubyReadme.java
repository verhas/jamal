package javax0.jamal.ruby;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;
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

    public static class MyProcessor {
        final Processor processor = new Processor("{", "}");

        public String process(String s) throws BadSyntax {
            return processor.process(Input.makeInput(s));
        }
    }

    private static void generateDoc(final String directory, final String fileName, final String ext) throws Exception {
        final var in = FileTools.getInput(directory + "/" + fileName + "." + ext + ".jam");
        final var processor = new Processor("{%", "%}");
        final var shell = Shell.getShell(processor, Shell.DEFAULT_RUBY_SHELL_NAME);
        shell.property("$processor", new MyProcessor());
        processor.defineGlobal(shell);
        final var result = processor.process(in);
        FileTools.writeFileContent(directory + "/" + fileName + "." + ext, result);
    }


    @Test
    void convertRubyReadme() throws Exception {
        generateAdoc("../jamal-ruby");
    }
}
