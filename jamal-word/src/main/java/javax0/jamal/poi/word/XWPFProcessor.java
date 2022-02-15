package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class XWPFProcessor {

    public void process(final String inputFile, final String outputFile) throws IOException, BadSyntax {
        final Path msWordPath = Paths.get(inputFile);
        final XWPFDocument document = new XWPFDocument(Files.newInputStream(msWordPath));
        try (final var processor = new javax0.jamal.engine.Processor()) {
            final XWPFInput input = new XWPFInput(document, null, document.getParagraphs());
            input.setStart(0, 0);
            while (input.notEmpty()) {
                //System.out.println("BEFORE PROCESSING:\n" + input.debugDoc());
                final String processed = processor.process(input);
                //System.out.println("AFTER PROCESSING:\n" + input.debugDoc());
                input.purgeSource();
                //System.out.println("AFTER PURGE:\n" + input.debugDoc());
                input.insert(processed);
                //System.out.println("AFTER REPLACE:\n" + input.debugDoc());
                input.step();
            }
            for (final var bodyElement : document.getBodyElements()) {
                if (bodyElement instanceof XWPFTable) {
                    final XWPFTable table = (XWPFTable) bodyElement;
                    for (final XWPFTableRow row : table.getRows()) {
                        for (final XWPFTableCell cell : row.getTableCells()) {
                            final var cellInput = new XWPFInput(document, cell, cell.getParagraphs());
                            cellInput.setStart(0, 0);
                            while (cellInput.notEmpty()) {
                                final String processed = processor.process(cellInput);
                                cellInput.purgeSource();
                                cellInput.insert(processed);
                                cellInput.step();
                            }
                        }
                    }
                }
            }
        }
        document.write(Files.newOutputStream(Paths.get(outputFile)));
    }

}
