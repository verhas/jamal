package javax0.jamal;

import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;

public class DocumentConverter {
    public static void convert(final String file) throws Exception {
        final var in = FileTools.getInput(file);
        final var processor = new Processor("{%", "%}");
        final var result = processor.process(in);
        final var output = file.substring(0, file.length() - ".jam".length());
        FileTools.writeFileContent(output, result);
    }
}
