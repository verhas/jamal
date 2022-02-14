package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Processor {

    public void process(final String inputFile, final String outputFile) throws IOException, BadSyntax {
        final Path msWordPath = Paths.get(inputFile);
        final XWPFDocument document = new XWPFDocument(Files.newInputStream(msWordPath));
        final XWPFInput input = new XWPFInput(document);
        input.setStart(0, 0);
        try (final var processor = new javax0.jamal.engine.Processor()) {
            while (input.paragraphEndIndex < document.getParagraphs().size() ||
                    input.runEndIndex < document.getParagraphs().get(input.paragraphEndIndex).getRuns().size()) {
                System.out.println("BEFORE PROCESSING:\n"+input.debugDoc());
                final String processed = processor.process(input);
                System.out.println("AFTER PROCESSING:\n"+input.debugDoc());
                input.purgeSource();
                System.out.println("AFTER PURGE:\n"+input.debugDoc());
                document.getParagraphs().get(input.paragraphStartIndex).getRuns().get(input.runStartIndex).setText(processed, 0);
                System.out.println("AFTER REPLACE:\n"+input.debugDoc());
                if (input.paragraphEndIndex >= document.getParagraphs().size() - 1 &&
                        input.runEndIndex >= document.getParagraphs().get(input.paragraphEndIndex).getRuns().size() - 1) {
                    break;
                }
                if (input.runEndIndex < document.getParagraphs().get(input.paragraphEndIndex).getRuns().size() - 1) {
                    input.setStart(input.paragraphEndIndex, input.runEndIndex + 1);
                } else {
                    input.paragraphEndIndex++;
                    while (input.paragraphEndIndex < document.getParagraphs().size()) {
                        if (document.getParagraphs().get(input.paragraphEndIndex).getRuns().size() > 0) {
                            input.setStart(input.paragraphEndIndex, 0);
                            break;
                        }
                        input.paragraphEndIndex++;
                    }
                }
            }
        }
        document.write(Files.newOutputStream(Paths.get(outputFile)));
    }
}
