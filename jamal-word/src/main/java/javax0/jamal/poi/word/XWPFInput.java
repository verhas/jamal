package javax0.jamal.poi.word;

import javax0.jamal.tools.Input;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;

import java.util.List;

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
            final var runs = paragraph.getRuns();
            final var nextLineRuns = paragraphs.get(paragraphStartIndex + 1).getRuns();
            for (int i = 0; i < nextLineRuns.size(); i++) {
                paragraph.insertNewRun(runs.size());
                final var destinationRun = runs.get(runs.size() - 1);
                final var sourceRun = nextLineRuns.get(i);
                destinationRun.setText(sourceRun.getText(0), 0);
                destinationRun.getCTR().set(sourceRun.getCTR());
            }
            document.removeBodyElement(paragraphStartIndex + 1);
        }
    }

    private void removeParagraphs() {
        for (int i = paragraphEndIndex - 1; i > paragraphStartIndex; i--) {
            document.removeBodyElement(i);
        }
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

    public boolean isExhausted() {
        return paragraphEndIndex >= paragraphs.size() - 1 &&
                runEndIndex >= paragraphs.get(paragraphEndIndex).getRuns().size() - 1;
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
            p.insertNewRun(0);
            final var run = p.getRuns().get(0);
            run.setText(lines[lines.length - 1], 0);
            run.getCTR().set(startRun.getCTR());
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
        if (!isExhausted()) {
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
