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

/**
 * Processes macros in a Word document.
 *
 * Word documents have to be processed in a special way.
 * The general model of Jamal is that the input and the output of the processor is text.
 * A Word document, however, contains textual information with formatting interleaved.
 * This processor processes the Word document in several steps to process the macros in the text and at the same time keep the formatting intact.
 *
 * This processor creates a single underlying Jamal processor and uses it many times.
 * It starts the processing of the Word document and fetches as many characters as minimally needed to finish one stage of the processing.
 * To do this there is a special implementation of the {@link javax0.jamal.api.Input} interface.
 * The implementation is {@link XWPFInput}.
 *
 * When a stage is finished the processor starts again for the rest of the document.
 *
 * The Word document internal structure is following:
 *
 * <ul>
 *     <li> A document is a list of body elements.</li>
 *     <li> A body element can be a paragraph or a table.</li>
 *     <li> The other type of body elements are not handled by this processor and are left intact.</li>
 *     <li> A table contains cells and the cells contain body elements recursively.</li>
 *     <li> A paragraph contains "runs".</li>
 *     <li> A run is a minimal amount of text that has the same character formatting.</li>
 *     <li> Paragraphs also have formatting, like indentation, but not character formatting.</li>
 * </ul>
 *
 * Note that this is a very simplified model.
 * The actual structure of a Word document is more complex, but for the understanding of the inner working of this processor it is sufficient.
 *
 * During the processing there are two different type of stages.
 *
 * <ol>
 *     <li>A stage that contains only text and no macros.
 *     <li>A stage that contains macros and text.
 * </ol>
 *
 * Every stage processes at least one run.
 * A stage must fetch more characters when there are opened and not yet closed macros.
 * In situations like that the input handler {@link XWPFInput} will fetch more runs.
 * When the stage sees that all the macros are closed it will stop fetching more characters.
 * More precisely, the input will tell that there are no more characters, even though there are more runs.
 * That way the stage finishes.
 *
 * The input will fetch no more run if it has reached the end of the document or if it has reached the end of the current paragraph and the next body element is a table.
 * If the stage has not finished at this point an error will occur.
 *
 * From the user point of view it means that all macros should be closed
 *
 * <ul>
 *     <li>before the end of the document,</li>
 *     <li>before the next table,</li>
 *     <li>withing the cell of a table.</li>
 * </ul>
 *
 * After a stage has finished, the processor will start again for the next stage.
 * It means that it has the same state, all defined macros, options, user defined macros and so on.
 *
 * That way a stage processes at least one top-level macro (a macro, which is not inside any other macro).
 * Since the input fetches one run at a time, it may happen that one run contains the end of the macro closing string and at the same time the start of the next macro opening string.
 * In that case the stage will not stop, because it means that the input has an already opened next macro.
 * That is why a stage processes <em>at least</em> one top-level macro.
 *
 * When a stage has finished the processor invokes the call-back objects, which were registered by the evaluated macros through the {@link XWPFContext#register(XWPFContext.DocxIntermediaryCallBack)} method.
 * The call-back objects are invoked in the order of their registration.
 *
 */
public class XWPFProcessor {

    final Processor processor;
    final XWPFContext xwpfContext;
    Position pos;
    XWPFDocument document;

    public XWPFProcessor() {
        this("{", "}");
    }

    public XWPFProcessor(final String open, final String close) {
        xwpfContext = new XWPFContext();
        this.processor = new Processor(open, close, xwpfContext);
    }

    public javax0.jamal.api.Processor getProcessor() {
        return processor;
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
            for( final var terminal : xwpfContext.getTerminals() ){
                terminal.setDocument(document);
                terminal.process();
            }
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
            for( final var intermediary : xwpfContext.getIntermediaries() ){
                intermediary.setParagraphStartIndex(input.paragraphStartIndex);
                intermediary.setRunStartIndex(input.runStartIndex);
                intermediary.setParagraphs(paragraphs);
                intermediary.setDocument(document);
                intermediary.process();
            }
            DebugTool.debugDoc("AFTER INTERMEDIARIES:\n", input);
            input.step();
            DebugTool.debugDoc("AFTER STEP:\n", input);
        }
    }

    /**
     * Add the {@code bodyElement} to the {@code paragraphs} cast to be a {@link XWPFParagraph} and fetch more
     * {@link IBodyElement}s so long as long there is any, and they are paragraphs.
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
