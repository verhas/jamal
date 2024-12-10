package javax0.jamal.poi.word;

import javax0.jamal.api.Position;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

import java.util.List;

/**
 * This class reads the characters from a Word document, and at the same time, it modifies the same document as the Jamal
 * processing goes on.
 * <p>
 * The class implements the {@link javax0.jamal.api.Input} interface and as such it can be used as an input for the
 * {@link javax0.jamal.api.Processor} implementation.
 * <p>
 * The trick in handling a Word document is that the document mixes text with formatting.
 * Reading the text and then processing it with Jamal is not an option, as it would lose the formatting.
 * <p>
 * Jamal processing can be split into separate steps. It always starts at the beginning of the input and works towards the end.
 * When a part is processed, Jamal is not returning.
 * This way, the processing of a Word file can start at the beginning of a Word document and fetch as much text as necessary.
 * <p>
 * When the processing needs some characters not taken from the Word file yet, the input handling can fetch those characters automatically.
 * It can happen when Jamal looks for a macro closing string or the first character after the macro opening string.
 * When the processing is finished, the processor returns the processed string, but at the same time, there may be characters left in the document not processed.
 * The input handling also knows where it started the processing and finished it.
 * Before processing the remaining document, the XWPFProcessor replaces the processed part with the result.
 * Following this approach, the document is replaced by the parts processed separately.
 * The processing steps use the same processor objects.
 * It means that all the macros remain valid.
 * If you define a macro in the document, you can use it later.
 * <p>
 * When the macro result is replaced, the formatting at the start of the part is used.
 * You cannot define a formatted text as the body of a macro.
 * The formatting inside a macro gets lost.
 * <p>
 * It is also a limitation of this approach that you cannot use, at least as for now, deferred actions.
 */
public class XWPFInput extends XWPFAbstractInput {

    final XWPFDocument document;
    private final XWPFTableCell cell;
    final List<XWPFParagraph> paragraphs;

    /**
     * Create a new input that will harvest the paragraphs so long as long it must. This input stops as soon as it can.
     * This way, the input can be processed in the smallest possible chunks. After that, the processor may continue with
     * the rest of the paragraphs and runs in the paragraphs.
     * <p>
     * The input is stored in paragraphs, and each paragraph contains runs. When there is a need for more input, this
     * implementation fetches the next run, and in case the runs in a paragraph are exhausted, then the next paragraph.
     * <p>
     * The "need for more input" happens when the processor looks for more macro closing strings.
     * It means that there are still some macro opening strings on the top level, which were not closed.
     * When all the macro openings are closed, the input does not go further but signals its end.
     * The processing there stops, and the processor returns to the {@link XWPFProcessor}. The processor then manages the
     * result, inserts it into the document, and then goes on with the remaining runs and paragraphs. For this it uses
     * the same input object restarting the input fetching calling {@link #setStart(int, int) setStart()}.
     * <p>
     * The paragraphs list may belong to the document or a table cell. There is no table cell when the paragraphs belong to the
     * main document. In this case the parameter {@code cell} is {@code null}. The
     * parameter {@code document} is never {@code null}.
     *
     * @param document   the document that we read and modify
     * @param cell       the cell that the paragraphs belong to, or {@code null} if the paragraphs belong to the document
     * @param paragraphs the actual paragraphs to process. It may be all the paragraphs that belong to the cell or
     *                   the document; it may be the paragraphs before the first table, the paragraphs between two
     *                   tables, or the paragraphs after the last table. This list is a copy of a slice of the paragraphs
     *                   list of the cell or document; therefore, the code mirrors every modification happening through the document or
     *                   cell object in this list.
     * @param pos        the position of the first paragraph in the list
     */
    public XWPFInput(XWPFDocument document, XWPFTableCell cell, List<XWPFParagraph> paragraphs, Position pos) {
        super("", pos);
        this.document = document;
        this.cell = cell;
        this.paragraphs = paragraphs;
    }

    /**
     * The four fields {@code paragraphStartIndex}, {@code paragraphEndIndex}, {@code runStartIndex}, and
     * {@code runEndIndex} keep track in the input the start and end position of the actual use of the document.
     *
     * <ul>
     *     <li>{@code paragraphStartIndex} is the index of the first paragraph that belongs to this input at
     *     the current processing step.</li>
     *     <li>{@code paragraphEndIndex} is the index of the last paragraph that belongs to this input</li>
     *     <li>{@code runStartIndex} is the index of the first run</li>
     *     <li>{@code runEndIndex} in the index of the last run</li>
     * </ul>
     *
     * <b>NOTE:</b> The indexes are INCLUSIVE. The {@code xxxEndIndex} is the index of the last element and not the
     * one after.
     */
    int paragraphStartIndex;
    int paragraphEndIndex;
    int runStartIndex;
    int runEndIndex;

    /**
     * Set the start of the processing at the given position. If the position is at the end of a paragraph, AFTER the last
     * run then the start position jumps to the start of the next paragraph in a loop till the end of the document or
     * until a non-empty paragraph is found.
     *
     * @param paragraphIndex the index of the paragraph where the processing should continue/start.
     * @param runIndex       the index of the run inside the paragraph where the processing should continue/start.
     */
    public void setStart(int paragraphIndex, int runIndex) {
        paragraphStartIndex = paragraphIndex;
        runStartIndex = runIndex;

        // if the runIndex is pointing already AFTER the last non-empty run then go to the start of the next paragraph
        // if there is next paragraph
        if (isEmptyAfter(paragraphs.get(paragraphStartIndex), runStartIndex) && paragraphStartIndex < paragraphs.size() - 1) {
            paragraphStartIndex++;
            runStartIndex = 0;
        }

        // skip empty paragraphs
        while (isEmpty(paragraphs.get(paragraphStartIndex)) && paragraphStartIndex < paragraphs.size() - 1) {
            paragraphStartIndex++;
            runStartIndex = 0;
        }

        paragraphEndIndex = paragraphStartIndex;
        runEndIndex = runStartIndex;
        if (paragraphStartIndex < paragraphs.size() && runStartIndex < paragraphs.get(paragraphStartIndex).getRuns().size()) {
            final var text = paragraphs.get(paragraphStartIndex).getRuns().get(runStartIndex).getText(0);
            input.append(text == null ? "" : text);
        }
    }

    /**
     * @param paragraph the paragraph to check
     * @param runIndex  the first run to check for emptyness
     * @return {@code true} if the paragraph is empty including and following the run {@code runIndex}, {@code false}
     * otherwise. It also returns {@code true} if the index is after the last run.
     */
    private static boolean isEmptyAfter(final XWPFParagraph paragraph, final int runIndex) {
        if (paragraph.getRuns().isEmpty()) {
            return true;
        }
        if (runIndex >= paragraph.getRuns().size()) {
            return true;
        }
        for (int i = runIndex; i < paragraph.getRuns().size(); i++) {
            final var run = paragraph.getRuns().get(i);
            if (run.getText(0) != null && !run.getText(0).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isLazy() {
        return true;
    }

    /**
     * @param paragraph to check if it is empty
     * @return {@code true} if the paragraph is empty, {@code false} otherwise.
     */
    private static boolean isEmpty(final XWPFParagraph paragraph) {
        if (paragraph.getRuns().isEmpty()) {
            return true;
        }
        for (XWPFRun run : paragraph.getRuns()) {
            if (run.getText(0) != null && !run.getText(0).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * This implementation return false if there are characters in the string builder buffer or if the
     * {@code paragraphEndIndex}, and {@code runEndIndex} values are pointing somewhere in the document and not at the
     * last run of the last paragraph.
     *
     * @return {@code true} if the input is at the end of the document, {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        if (input.length() > 0) {
            return false;
        }
        return paragraphEndIndex == paragraphs.size() - 1 && runEndIndex == paragraphs.get(paragraphEndIndex).getRuns().size() - 1;
    }

    /**
     * This implementation fetches the character from the string builder buffer. If the {@code index}
     * index-able range of the buffer (non-negative) then the buffer is filled from the current position.
     * The algorithm always fetched one run from the document and appends to the buffer.
     * <p>
     * If there is no more runs in the paragraphs then the method throws a {@link StringIndexOutOfBoundsException} just
     * like the implementation in {@link CharSequence#charAt(int)}.
     *
     * @param index the index of the character to get.
     * @return the character at the given index.
     */
    @Override
    public char charAt(int index) {
        if (index < input.length()) {
            return input.charAt(index);
        }
        while (index >= input.length()) {
            final var l = input.length();
            appendOneRun();
            if (empty()) {// nothing was appended
                throw new StringIndexOutOfBoundsException(index);
            }
        }
        return input.charAt(index);
    }

    /**
     * This implementation handles the case when {@code before} is {@code -1}. In that case the value of the
     * {@link #insert(String)} is not {@code -1} then it is returned. This is the case when the string can
     * be found in the string builder buffer.
     * <p>
     * If the string is not found in the buffer, then the method appends the strings of the consecutive runs to the
     * buffer until the string is there, or there are no more runs. The return value is the index of the string in the
     * buffer, or {@code -1} if the string is not found even after all the possible runs are appended.
     *
     * @param s      the string to be found
     * @param before is a hint. If {@code s} can be found before {@code before} then it is okay. If it
     *               cannot be found then before the position the return value may be -1. In
     * @return the index of the first character of the string {@code s} in the buffer, or {@code -1} if the string is
     * not in the buffer.
     */
    @Override
    public int indexOf(String s, int before) {
        if (before == -1) {
            int index = indexOf(s);
            while (index == -1 && thereAreMOreRuns()) {
                appendOneRun();
                index = indexOf(s);
            }
            return index;
        } else {
            return indexOf(s);
        }
    }

    /**
     * This implementation of {@link javax0.jamal.api.Input#indexOf(String)} takes care of the fact that whenever
     * something is calling this method it may be an error if the string is not found.
     * It is not always the case, but whenever the code is looking for the macro opening or closing string it is.
     * A typical example is, when the string buffer contains
     *
     * <pre>{@code
     *     <%@define j/Jama%><
     * }</pre>
     * <p>
     * when {@code <%} is the macro opening, and {@code %>} is the macro closing string.
     * <p>
     * In this case the next character in the doc file may be {@code %}.
     * In that case default implementation returns {@code -1}, which is not correct. That would lead to the
     * evaluation of the {@code <%j%>} as pure text instead of interpreting it as a macro. The actual flow would be
     * interpreting {@code <} as a normal character at the end of the string buffer, closing the processor and then the
     * {@link XWPFProcessor} would restart the processor again an star the processing with the characters {@code %j%>},
     * which, again, is just normal, non-macro character sequence.
     * <p>
     * To handle this situation, this implementation appends new runs to the buffer so long as long
     * <ul>
     *     <li>the buffer is shorter than the searched string,</li>
     *     <li>there is something to append, in other words the doc file still has characters left,</li>
     *     <li>the searched string {@code str} starts with the characters, which are already in the buffer.</li>
     * </ul>
     * <p>
     *
     * @param str the string we are looking for
     * @return the index of the string in the buffer, or {@code -1} if the string is not in the buffer or at the
     * document, as described above.
     */
    @Override
    public int indexOf(String str) {
        while (input.length() < str.length() && thereAreMOreRuns() && str.startsWith(input.toString())) {
            appendOneRun();
        }
        return input.indexOf(str);
    }

    private boolean thereAreMOreRuns() {
        return !(paragraphEndIndex == paragraphs.size() - 1 && runEndIndex > paragraphs.get(paragraphEndIndex).getRuns().size() - 1);
    }

    /**
     * @return {@code true} if the input is empty and the processing can continue, or {@code false} when the input is
     * empty and the processing can be started from the position where the last processing ended.
     */
    public boolean empty() {
        return paragraphEndIndex >= paragraphs.size() - 1 &&
                runEndIndex >= paragraphs.get(paragraphEndIndex).getRuns().size() - 1 &&
                input.length() == 0;
    }

    /**
     * Append one run to the buffer and adjust the {@code paragraphEndIndex} and {@code runEndIndex} values.
     */
    private void appendOneRun() {
        final var endRuns = paragraphs.get(paragraphEndIndex).getRuns();
        runEndIndex++;
        while (runEndIndex < endRuns.size() && endRuns.get(runEndIndex).getText(0) == null) {
            runEndIndex++;
        }
        if (runEndIndex < endRuns.size()) {
            input.append(endRuns.get(runEndIndex).getText(0));
        } else while (paragraphEndIndex + 1 < paragraphs.size()) {
            paragraphEndIndex++;
            runEndIndex = 0;
            input.append("\n");
            final var endRunsNext = paragraphs.get(paragraphEndIndex).getRuns();
            if (!endRunsNext.isEmpty()) {
                final var text = endRunsNext.get(0).getText(0);
                input.append(text == null ? "" : text);
                break;
            }
        }
    }

    /**
     * Delete the runs and the paragraphs that were used up in this run, except the very first run.
     * <p>
     * When the processing returns the result of the processing will replace the original runs.
     * This happens in two steps. First the runs are deleted and then the new string is inserted
     * (see {@link #insert(String)}). The insertion is done in several steps, but the first step is
     * to update the text of the very first run of the original runs of this processing. The new runs
     * added will inherit the properties of the very first run.
     * <p>
     * This process is the reason why this method deletes all but the first runs of this processing step.
     * The very first run is needed to keep the formatting information.
     */
    public void purgeSource() {
        if (paragraphStartIndex == paragraphEndIndex) {
            purgeInParagraph();
        } else {
            purgeInMultipleParagraphs();
        }
        paragraphEndIndex = paragraphStartIndex;
        runEndIndex = runStartIndex;
    }

    /**
     * Remove the runs between the {@code runStartIndex+1} and {@code runEndIndex} values.
     * It means that we have to remove some runs from the first paragraph from the end, then the whole paragraphs
     * before the last one, and finally the runs that are on the last paragraph before the {@code runEndIndex}.
     */
    private void purgeInMultipleParagraphs() {
        purgeEndOfParagraph();
        removeParagraphs();
        purgeStartOfParagraph();
    }

    /**
     * Remove the runs from the end of the paragraph where the processing started. The first run pointed by
     * {@code runStartIndex} remains, because it is needed for the formatting and in this very first run only the
     * text is replaced, but all other later runs in this paragraph are removed.
     * <p>
     * The removing starts from the end because if I remove a run in the inside then the indices of the latter runs
     * decrease and then the code would need to remove the same index number of runs again and again. It is more
     * intuitive to remove from the end and then decrease the index, going backward.
     */
    private void purgeEndOfParagraph() {
        for (int i = paragraphs.get(paragraphStartIndex).getRuns().size() - 1; i > runStartIndex; i--) {
            paragraphs.get(paragraphStartIndex).removeRun(i);
        }
    }

    /**
     * Remove the paragraphs between the {@code paragraphStartIndex+1} and {@code paragraphEndIndex-1} values.
     * The first and the last paragraph remains. These paragraphs contain runs, which belong to a different processing
     * step, and now we should not alter them. Those paragraphs lose only the runs, which belong to this processing
     * step. See the methods {@link #purgeStartOfParagraph()} and {@link #purgeEndOfParagraph()}.
     */
    private void removeParagraphs() {
        for (int i = paragraphEndIndex - 1; i > paragraphStartIndex; i--) {
            removeParagraph(i);
        }
    }

    /**
     * Remove the runs from the start of the last paragraph that belongs to this processing step.
     */
    private void purgeStartOfParagraph() {
        if (runEndIndex == paragraphs.get(paragraphStartIndex + 1).getRuns().size() - 1) {
            removeParagraph(paragraphStartIndex + 1);
        } else {
            for (int i = runEndIndex; i >= 0; i--) {
                paragraphs.get(paragraphStartIndex + 1).removeRun(i);
            }
            joinParagraphs();
        }
    }

    /**
     * When the runs of the source are removed (except the first run), then the result of the processing should be
     * inserted into the remaining run and into subsequent runs. Since the output of Jamal is unformatted text, and it
     * inherits the formatting of the first run of the source, subsequent runs also mean subsequent paragraphs.
     * <p>
     * For example:
     *
     * <pre>{@code
     *  {@define here=HERE}
     *  This is the first run of the source. {here
     *  } we have the next paragraph.
     * }</pre>
     * <p>
     * Should result
     *
     * <pre>{@code
     *
     *  This is the first run of the source. HERE we have the next paragraph.
     * }</pre>
     * <p>
     * To have that the first and the next paragraph has to be joined first. If the result of the processing is
     * multi-line then multiple paragraphs will later be inserted.
     */
    private void joinParagraphs() {
        if (paragraphStartIndex + 1 < paragraphs.size()) {
            final var paragraph = paragraphs.get(paragraphStartIndex);
            for (XWPFRun sourceRun : paragraphs.get(paragraphStartIndex + 1).getRuns()) {
                final var destinationRun = paragraph.createRun();
                copyRun(sourceRun, destinationRun);
            }
            removeParagraph(paragraphStartIndex + 1);
        }
    }

    /**
     * Copy the formatting and the text from the source run to the destination run.
     *
     * @param sourceRun      the source run to copy from
     * @param destinationRun the destination run to copy to
     */
    private void copyRun(final XWPFRun sourceRun, final XWPFRun destinationRun) {
        copyRunFormatting(sourceRun, destinationRun);
        copyRunText(sourceRun, destinationRun);
    }

    /**
     * Copy the text from the source to the destination run.
     *
     * @param sourceRun      the source run to copy from
     * @param destinationRun the destination run to copy to
     */
    private void copyRunText(final XWPFRun sourceRun, final XWPFRun destinationRun) {
        destinationRun.setText(sourceRun.getText(0), 0);
    }

    /**
     * Copy the formatting from the source run to the destination run.
     *
     * @param sourceRun      the source run to copy from
     * @param destinationRun the destination run to copy to
     */
    private void copyRunFormatting(final XWPFRun sourceRun, final XWPFRun destinationRun) {
        destinationRun.getCTR().set(sourceRun.getCTR());
    }

    /**
     * Remove the paragraph at the given index.
     * <p>
     * This method removes the paragraph from the document as well as from the list of the paragraphs.
     *
     * @param i the index of the paragraph to be removed
     */
    private void removeParagraph(int i) {
        document.removeBodyElement(document.getPosOfParagraph(paragraphs.get(i)));
        paragraphs.remove(i);
    }

    /**
     * Remove the runs when the purging is inside a single paragraph. This happens when the processing of the input
     * started and ended in the same paragraph.
     */
    private void purgeInParagraph() {
        for (int i = runEndIndex; i > runStartIndex; i--) {
            paragraphs.get(paragraphStartIndex).removeRun(i);
        }
    }

    /**
     * Insert the {@code text} into the paragraph at the given {@code index}. The text will overwrite the existing
     * run at the position {@code runStartIndex} and will inserts consecutive paragraphs if the text contains
     * new lines.
     *
     * @param text the text to insert
     */
    public void insert(String text) {
        final var lastNl = text.lastIndexOf('\n') == text.length() - 1;
        final var startParagraph = paragraphs.get(paragraphStartIndex);
        final var startRuns = startParagraph.getRuns();
        final var startRun = startRuns.get(runStartIndex);
        final var lines = text.split("\n");
        startRun.setText(lines[0], 0);
        if (lines.length > 1) {
            final var p = newParagraph(paragraphStartIndex);

            for (int i = runEndIndex + 1, j = 0; i < paragraphs.get(paragraphStartIndex).getRuns().size(); i++, j++) {
                final var run = p.insertNewRun(j);
                run.setText(startRuns.get(i).getText(0), 0);
                copyRunFormatting(startRuns.get(i), run);
            }

            for (int i = paragraphs.get(paragraphStartIndex).getRuns().size() - 1; i > runEndIndex; i--) {
                startParagraph.removeRun(i);
            }
        }
        for (int i = 1; i < lines.length + (lastNl ? 0 : -1); i++) {
            final var p = newParagraph(paragraphStartIndex + i - 1);
            final var run = p.insertNewRun(0);
            copyRunFormatting(startRun, run);
            run.setText(lines[i], 0);
        }
        if (lines.length > 1 && !lastNl) {
            final var p = paragraphs.get(paragraphStartIndex + lines.length - 1);
            final var run = p.insertNewRun(0);
            copyRunFormatting(startRun, run);
            run.setText(lines[lines.length - 1], 0);
            runEndIndex = 1;
        }
        paragraphEndIndex = paragraphStartIndex + lines.length - 1;
    }

    /**
     * Create a new empty paragraph and insert it at after the paragraph at the given index.
     * If the index points to the last paragraph then the new paragraph is appended to the end of the document or the
     * cell.
     * <p>
     * The paragraph is inserted into the list of the paragraph into a table cell if the field {@code cell} is not null.
     *
     * @param afterTheParagraph the index of the paragraph after which the new paragraph will be inserted
     * @return the new paragraph that was inserted
     */
    private XWPFParagraph newParagraph(final int afterTheParagraph) {
        final var cursor = getCursorAfterParagraph(afterTheParagraph);
        final XWPFParagraph p;
        if (cursor == null) {
            if (cell == null) {
                p = document.createParagraph();
            } else {
                p = cell.addParagraph();
            }
            paragraphs.add(p);
        } else {
            if (cell == null) {
                p = document.insertNewParagraph(cursor);
            } else {
                p = cell.insertNewParagraph(cursor);
            }
            paragraphs.add(afterTheParagraph + 1, p);
        }
        final CTP ctp = (CTP) paragraphs.get(afterTheParagraph).getCTP().copy();
        deleteAllRuns(ctp);
        p.getCTP().set(ctp);
        return p;
    }

    /**
     * Delete all runs from the CTP object.
     * <p>
     * Apache POI does not provide a method to copy all formatting of a paragraph to another. What can be done is to
     * set the CTP object of the paragraph to the copy of the CTP object of the other paragraph. This, however, contains
     * the runs as well. This method deletes all runs from the CTP object.
     * <p>
     * Interestingly the runs do NOT appear in the paragraph object as runs after the copy, but they are in the CTP and
     * when new runs are added they are appended to the already existing runs, which are ONLY in the CTP.
     *
     * @param p the CTP object that has the runs to be deleted
     */
    private void deleteAllRuns(CTP p) {
        while (!p.getRList().isEmpty()) {
            p.getRList().remove(0);
        }
    }

    /**
     * Get the cursor after the paragraph at the given index or {@code null} if the index points to the last paragraph.
     *
     * @param paragraphIndex the index of the paragraph after which the cursor is requested
     * @return the cursor or  {@code null} if the index points to the last paragraph.
     */
    private XmlCursor getCursorAfterParagraph(int paragraphIndex) {
        if (paragraphs.size() == paragraphIndex + 1) {
            return null;
        } else {
            return paragraphs.get(paragraphIndex + 1).getCTP().newCursor();
        }
    }

    /**
     * After finishing a processing step the {@code paragraphStartIndex} and {@code runStartIndex} to the next
     * processable run. This step jumps over all empty paragraphs. After calling this method the caller has to call
     * {@link #empty()} to check if there is any more processable input in the paragraph list at all.
     */
    public void step() {
        if (!empty()) {
            if (runEndIndex < paragraphs.get(paragraphEndIndex).getRuns().size() - 1) {
                setStart(paragraphEndIndex, runEndIndex + 1);
            } else {
                while (paragraphEndIndex < paragraphs.size() - 1) {
                    paragraphEndIndex++;
                    if (!paragraphs.get(paragraphEndIndex).getRuns().isEmpty()) {
                        setStart(paragraphEndIndex, 0);
                        break;
                    }
                }
            }
        }
    }
}
