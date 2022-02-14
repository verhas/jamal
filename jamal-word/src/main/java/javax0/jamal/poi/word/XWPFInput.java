package javax0.jamal.poi.word;

import javax0.jamal.tools.Input;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class XWPFInput extends Input {
    final XWPFDocument document;

    public XWPFInput(XWPFDocument document) {
        super();
        this.document = document;
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
        getSB().append(document.getParagraphs().get(paragraphIndex).getRuns().get(runIndex).getText(0));
    }

    @Override
    public boolean isEmpty() {
        if (getSB().length() > 0) {
            return false;
        }
        return paragraphEndIndex == document.getParagraphs().size() - 1 && runEndIndex == document.getParagraphs().get(paragraphEndIndex).getRuns().size() - 1;
    }

    @Override
    public int indexOf(String s, int before) {
        if (before == -1) {
            int index = indexOf(s);
            while (index == -1 && !(paragraphEndIndex == document.getParagraphs().size() - 1 && runEndIndex == document.getParagraphs().get(paragraphEndIndex).getRuns().size() - 1)) {
                if (runEndIndex + 1 < document.getParagraphs().get(paragraphEndIndex).getRuns().size()) {
                    runEndIndex++;
                    getSB().append(document.getParagraphs().get(paragraphEndIndex).getRuns().get(runEndIndex).getText(0));
                } else while (paragraphEndIndex + 1 < document.getParagraphs().size()) {
                    paragraphEndIndex++;
                    runEndIndex = 0;
                    getSB().append("\n");
                    if (0 < document.getParagraphs().get(paragraphEndIndex).getRuns().size()) {
                        getSB().append(document.getParagraphs().get(paragraphEndIndex).getRuns().get(0).getText(0));
                        break;
                    }
                }
                index = indexOf(s);
            }
            return index;
        } else {
            return indexOf(s);
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
        if (paragraphStartIndex + 1 < document.getParagraphs().size()) {
            final var paragraph = document.getParagraphs().get(paragraphStartIndex);
            final var runs = paragraph.getRuns();
            final var nextLineRuns =document.getParagraphs().get(paragraphStartIndex + 1).getRuns();
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
        if (runEndIndex == document.getParagraphs().get(paragraphStartIndex + 1).getRuns().size() - 1) {
            document.removeBodyElement(paragraphStartIndex + 1);
        } else {
            for (int i = runEndIndex; i >= 0; i--) {
                document.getParagraphs().get(paragraphStartIndex + 1).removeRun(i);
            }
            joinParagraphs();
        }
    }

    private void purgeEndOfParagraph() {
        for (int i = document.getParagraphs().get(paragraphStartIndex).getRuns().size() - 1; i > runStartIndex; i--) {
            document.getParagraphs().get(paragraphStartIndex).removeRun(i);
        }
    }

    private void purgeInParagraph() {
        for (int i = runEndIndex; i >= runStartIndex + 1; i--) {
            document.getParagraphs().get(paragraphStartIndex).removeRun(i);
        }
        runEndIndex = runStartIndex + 1;
        if (runEndIndex == document.getParagraphs().get(paragraphStartIndex).getRuns().size()) {
            runEndIndex = 0;
        }
    }

    public String debugDoc() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            final String paragraphSString = paragraphStartIndex == i ? "S*" : "  ";
            final String paragraphEString = paragraphEndIndex == i ? "E*" : "  ";
            sb.append(paragraphSString).append(paragraphEString).append("Paragraph ").append(i).append(":\n");
            for (int j = 0; j < document.getParagraphs().get(i).getRuns().size(); j++) {
                final String runSString = runStartIndex == j && paragraphStartIndex == i ? "S*" : "  ";
                final String runEString = runEndIndex == j && paragraphEndIndex == i ? "E*" : "  ";
                sb.append(runSString).append(runEString).append("Run ").append(j).append(": ").append(document.getParagraphs().get(i).getRuns().get(j).getText(0)).append("\n");
            }
        }
        return sb.toString();
    }

    public String toDebugString() {
        final var ts = new StringBuilder(getSB());
        for (int j = runEndIndex + 1; j < document.getParagraphs().get(paragraphEndIndex).getRuns().size(); j++) {
            ts.append("[").append(document.getParagraphs().get(paragraphEndIndex).getRuns().get(j).getText(0)).append("]");
        }
        for (int i = paragraphEndIndex + 1; i < document.getParagraphs().size(); i++) {
            ts.append("P`");
            for (int j = 0; j < document.getParagraphs().get(i).getRuns().size(); j++) {
                ts.append("[").append(document.getParagraphs().get(i).getRuns().get(j).getText(0)).append("]");
            }
            ts.append("`\n");
        }
        return ts.toString();
    }
}
