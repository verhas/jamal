package javax0.jamal.poi.word;

import javax0.jamal.tools.Input;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;

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
public class XWPFInput extends Input {
    final XWPFDocument document;
    final List<XWPFParagraph> paragraphs;

    public XWPFInput(XWPFDocument document) {
        super();
        this.document = document;
        paragraphs = document.getParagraphs();
    }

    int paragraphStartIndex;
    int paragraphEndIndex;
    int runStartIndex;
    int runEndIndex;

    public void setStart(int paragraphIndex, int runIndex) {
        paragraphStartIndex = paragraphIndex;
        runStartIndex = runIndex;
        paragraphEndIndex = paragraphIndex;
        runEndIndex = runIndex;
        getSB().setLength(0);
        getSB().append(paragraphs.get(paragraphIndex).getRuns().get(runIndex).getText(0));
    }

    @Override
    public boolean isEmpty() {
        if (getSB().length() > 0) {
            return false;
        }
        return paragraphEndIndex == paragraphs.size() - 1 && runEndIndex == paragraphs.get(paragraphEndIndex).getRuns().size() - 1;
    }

    @Override
    public char charAt(int index) {
        if (index < getSB().length()) {
            return getSB().charAt(index);
        }
        while (index >= getSB().length()) {
            final var l = getSB().length();
            appendOneRun();
            if (l == getSB().length()) {
                throw new StringIndexOutOfBoundsException(index);
            }
        }
        return getSB().charAt(index);
    }

    @Override
    public int indexOf(String s, int before) {
        if (before == -1) {
            int index = indexOf(s);
            while (index == -1 && !(paragraphEndIndex == paragraphs.size() - 1 && runEndIndex == paragraphs.get(paragraphEndIndex).getRuns().size() - 1)) {
                appendOneRun();
                index = indexOf(s);
            }
            return index;
        } else {
            return indexOf(s);
        }
    }

    private void appendOneRun() {
        if (runEndIndex + 1 < paragraphs.get(paragraphEndIndex).getRuns().size()) {
            runEndIndex++;
            getSB().append(paragraphs.get(paragraphEndIndex).getRuns().get(runEndIndex).getText(0));
        } else while (paragraphEndIndex + 1 < paragraphs.size()) {
            paragraphEndIndex++;
            runEndIndex = 0;
            getSB().append("\n");
            if (0 < paragraphs.get(paragraphEndIndex).getRuns().size()) {
                getSB().append(paragraphs.get(paragraphEndIndex).getRuns().get(0).getText(0));
                break;
            }
        }
    }

    /**
     * Delete the runs and the paragraphs that were used up in this run, except the very first run.
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

    private void purgeInMultipleParagraphs() {
        purgeEndOfParagraph();
        removeParagraphs();
        purgeStartOfParagraph();
    }

    private void joinParagraphs() {
        if (paragraphStartIndex + 1 < paragraphs.size()) {
            final var paragraph = paragraphs.get(paragraphStartIndex);
            for (XWPFRun sourceRun : paragraphs.get(paragraphStartIndex + 1).getRuns()) {
                final var destinationRun = paragraph.createRun();
                destinationRun.getCTR().set(sourceRun.getCTR());
                destinationRun.setText(sourceRun.getText(0), 0);
            }
            removeParagraph(paragraphStartIndex + 1);
        }
    }

    private void removeParagraphs() {
        for (int i = paragraphEndIndex - 1; i > paragraphStartIndex; i--) {
            removeParagraph(i);
        }
    }

    private void removeParagraph(int i) {
        document.removeBodyElement(document.getPosOfParagraph(paragraphs.get(i)));
    }

    private void purgeStartOfParagraph() {
        if (runEndIndex == paragraphs.get(paragraphStartIndex + 1).getRuns().size() - 1) {
            document.removeBodyElement(paragraphStartIndex + 1);
        } else {
            for (int i = runEndIndex; i >= 0; i--) {
                paragraphs.get(paragraphStartIndex + 1).removeRun(i);
            }
            joinParagraphs();
        }
    }

    private void purgeEndOfParagraph() {
        for (int i = paragraphs.get(paragraphStartIndex).getRuns().size() - 1; i > runStartIndex; i--) {
            paragraphs.get(paragraphStartIndex).removeRun(i);
        }
    }

    private void purgeInParagraph() {
        for (int i = runEndIndex; i >= runStartIndex + 1; i--) {
            paragraphs.get(paragraphStartIndex).removeRun(i);
        }
        runEndIndex = runStartIndex + 1;
        if (runEndIndex == paragraphs.get(paragraphStartIndex).getRuns().size()) {
            runEndIndex = 0;
        }
    }

    boolean notEmpty() {
        return paragraphEndIndex < paragraphs.size() - 1 ||
                runEndIndex < paragraphs.get(paragraphEndIndex).getRuns().size() - 1;
    }

    public String debugDoc() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paragraphs.size(); i++) {
            final String paragraphSString = paragraphStartIndex == i ? "S*" : "  ";
            final String paragraphEString = paragraphEndIndex == i ? "E*" : "  ";
            sb.append(paragraphSString).append(paragraphEString).append("(").append(i).append(":");
            for (int j = 0; j < paragraphs.get(i).getRuns().size(); j++) {
                final String runSString = runStartIndex == j && paragraphStartIndex == i ? "S*" : "";
                final String runEString = runEndIndex == j && paragraphEndIndex == i ? "E*" : "";
                sb.append(runSString).append(runEString).append("[").append(j).append("/").append(paragraphs.get(i).getRuns().get(j).getCTR().sizeOfTArray()).append(":");
                for (int k = 0; k < paragraphs.get(i).getRuns().get(j).getCTR().sizeOfTArray(); k++) {
                    sb.append(paragraphs.get(i).getRuns().get(j).getText(k));
                }
                sb.append("]");
            }
            sb.append(")\n");
        }
        return sb.toString();
    }

    public String toDebugString() {
        final var ts = new StringBuilder(getSB());
        for (int j = runEndIndex + 1; j < paragraphs.get(paragraphEndIndex).getRuns().size(); j++) {
            ts.append("[").append(paragraphs.get(paragraphEndIndex).getRuns().get(j).getText(0)).append("]");
        }
        for (int i = paragraphEndIndex + 1; i < paragraphs.size(); i++) {
            ts.append("P`");
            for (int j = 0; j < paragraphs.get(i).getRuns().size(); j++) {
                ts.append("[").append(paragraphs.get(i).getRuns().get(j).getText(0)).append("]");
            }
            ts.append("`\n");
        }
        return ts.toString();
    }

    private static void dumpParagraph(XWPFParagraph paragraph) {
        System.out.println("Paragraph: " + paragraph.getText());
        for (XWPFRun run : paragraph.getRuns()) {
            System.out.println("Run: " + run.getText(0));
        }
    }

    public void insert(String text) {
        final var lastNl = text.lastIndexOf('\n') == text.length() - 1;
        final var startParagraph = paragraphs.get(paragraphStartIndex);
        final var startRuns = startParagraph.getRuns();
        final var startRun = startParagraph.getRuns().get(runStartIndex);
        final var lines = text.split("\n");
        startRun.setText(lines[0], 0);
        if (lines.length > 1) {
            final XmlCursor cursor = getCursorAfterParagraph(paragraphStartIndex);
            final var p = cursor == null ? document.createParagraph() : document.insertNewParagraph(cursor);

            for (int i = runEndIndex + 1, j = 0; i < paragraphs.get(paragraphStartIndex).getRuns().size(); i++, j++) {
                final var run = p.insertNewRun(j);
                run.setText(startRuns.get(i).getText(0), 0);
                run.getCTR().set(startRuns.get(i).getCTR());
            }
            for (int i = paragraphs.get(paragraphStartIndex).getRuns().size() - 1; i > runEndIndex; i--) {
                startParagraph.removeRun(i);
            }
        }
        for (int i = 1; i < lines.length + (lastNl ? 0 : -1); i++) {
            final var cursor = getCursorAfterParagraph(paragraphStartIndex + i - 1);
            final var p = cursor == null ? document.createParagraph() : document.insertNewParagraph(cursor);
            final var run = p.insertNewRun(0);
            run.getCTR().set(startRun.getCTR());
            run.setText(lines[i], 0);
        }
        if (lines.length > 1 && !lastNl) {
            final var p = paragraphs.get(paragraphStartIndex + lines.length - 1);
            final var run = p.insertNewRun(0);
            run.getCTR().set(startRun.getCTR());
            run.setText(lines[lines.length - 1], 0);
            runEndIndex = 1;
            paragraphEndIndex = paragraphStartIndex + lines.length - 1;
        }
    }

    private XmlCursor getCursorAfterParagraph(int paragraphIndex) {
        final XmlCursor cursor;
        if (paragraphs.size() == paragraphIndex + 1) {
            return null;
        } else {
            return paragraphs.get(paragraphIndex + 1).getCTP().newCursor();
        }
    }

    public void step() {
        if (notEmpty()) {
            if (runEndIndex < paragraphs.get(paragraphEndIndex).getRuns().size() - 1) {
                setStart(paragraphEndIndex, runEndIndex + 1);
            } else {
                while (paragraphEndIndex < paragraphs.size() - 1) {
                    paragraphEndIndex++;
                    if (paragraphs.get(paragraphEndIndex).getRuns().size() > 0) {
                        setStart(paragraphEndIndex, 0);
                        break;
                    }
                }
            }
        }
    }
}
