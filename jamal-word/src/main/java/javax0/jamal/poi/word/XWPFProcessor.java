package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XWPFProcessor {

    final Processor processor;
    Position pos;
    XWPFDocument document;

    public XWPFProcessor() {
        this.processor = new javax0.jamal.engine.Processor();
    }

    public XWPFProcessor(final String open, final String close) {
        this.processor = new javax0.jamal.engine.Processor(open, close);
    }

    public void process(final String inputFile, final String outputFile) throws IOException, BadSyntax {
        final Path inputPath = Paths.get(inputFile);
        final Path outputPath = Paths.get(outputFile);
        process(inputPath, outputPath);
    }

    public void process(final Path inputPath, final Path outputPath) throws IOException, BadSyntax {
        pos = new Position(inputPath.toString(), 0, 0);
        document = new XWPFDocument(Files.newInputStream(inputPath));
        try {
            process(null, document.getBodyElements());
            if (outputPath != null) {
                document.write(Files.newOutputStream(outputPath));
            }
        } finally {
            processor.close();
        }
    }

    /**
     * Process the body elements one after the other. If the first body element is a table then the processing is
     * recursive for all the body elements for each cell. If the first body element is a paragraph then the processing
     * is performed for the paragraphs till the first non-paragraph body element or the end of the document.
     * <p>
     * This processing is done in a loop for the whole document.
     *
     * @param topCell      is the cell from which the body elements are collected or {@code null} if the body elements are
     *                     collected from the whole document.
     * @param bodyElements are the body elements to be processed.
     * @throws BadSyntax if the underlying Jamal processing throws an exception.
     */
    private void process(XWPFTableCell topCell, List<IBodyElement> bodyElements) throws BadSyntax {

        Iterator<IBodyElement> iterator = new ConcurrentIterator<>(bodyElements);
        var bodyElement = iterator.next();
        while (true) {
            if (bodyElement instanceof XWPFTable) {
                final XWPFTable table = (XWPFTable) bodyElement;
                for (final XWPFTableRow row : table.getRows()) {
                    for (final XWPFTableCell cell : row.getTableCells()) {
                        final var cellBodyElements = new ArrayList<>(cell.getBodyElements());
                        process(cell, cellBodyElements);
                    }
                }
                if (iterator.hasNext()) {
                    bodyElement = iterator.next();
                } else {
                    break;
                }
            } else if (bodyElement instanceof XWPFParagraph) {
                final var paragraphs = new ArrayList<XWPFParagraph>();
                bodyElement = collectsParagraphs(iterator, bodyElement, paragraphs);
                processParagraphs(topCell, paragraphs);
                if (bodyElement == null) {
                    break;
                }
            }
        }
    }

    /**
     * Process the collected paragraphs.
     * <p>
     * The processing goes for all paragraphs between tables, or before the first and after the last paragraph.
     * When a table is found then the processing starts for the paragraphs for each cell. If the cell also contains
     * a table then the processing is performed the same way as for the document. First the paragraphs before the first
     * table, then the paragraphs between the tables with the paragraphs inside the table cells and finally the
     * paragraphs after the last table.
     * <p>
     * This method performs the processing for one run of paragraphs.
     *
     * @param cell       the cell that the paragraphs are collected from or {@code null} if the paragraphs are collected
     *                   from the document
     * @param paragraphs the paragraphs to be processed
     * @throws BadSyntax if the processing throws a bad syntax exception
     */
    private void processParagraphs(final XWPFTableCell cell, final List<XWPFParagraph> paragraphs) throws BadSyntax {
        final XWPFInput input = new XWPFInput(document, cell, paragraphs, pos);
        input.setStart(0, 0);
        while (!input.empty()) {
            DebugTool.debugDoc("BEFORE PROCESSING:\n", input);
            final String processed = processor.process(input);
            DebugTool.debugDoc("AFTER PROCESSING:\n", input);
            input.purgeSource();
            DebugTool.debugDoc("AFTER PURGE:\n", input);
            input.insert(processed);
            DebugTool.debugDoc("AFTER REPLACE:\n", input);
            input.step();
        }
    }

    /**
     * Add the {@code bodyElement} to the {@code paragraphs} cast to be a {@link XWPFParagraph} and fetch more
     * {@link IBodyElement}s so long as long there is any and they are paragraphs.
     * <p>
     * If there is a non-paragraph body element then it is not added to the list and the already fetched non-paragraph
     * {@code bodyElement} is returned. If there is no more body element {@code null} is returned.
     *
     * @param iterator    the iterator that returns the body elements
     * @param bodyElement the last body element that was fetched during the last collection or the first paragraph
     *                    after a table or some other non-paragraph body element.
     * @param paragraphs  the list of paragraphs where the paragraph body elements are collected
     * @return the first not collected body element or {@code null} if there is no more body element.
     */
    private IBodyElement collectsParagraphs(final Iterator<IBodyElement> iterator, IBodyElement bodyElement, final List<XWPFParagraph> paragraphs) {
        paragraphs.add((XWPFParagraph) bodyElement);
        while (iterator.hasNext()) {
            bodyElement = iterator.next();
            if (bodyElement instanceof XWPFParagraph) {
                paragraphs.add((XWPFParagraph) bodyElement);
            } else {
                return bodyElement;
            }
        }
        return null;
    }
}
