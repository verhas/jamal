package javax0.jamal.poi.word;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.util.List;

public class DebugTool {
    private static int counter = 0;

    public static void debugDoc(final String prefix, final XWPFInput input) {
        counter++;
        final var sb = new StringBuilder(counter + ": ");
        final var SP = input.paragraphs.get(Math.min(input.paragraphStartIndex,input.paragraphs.size()-1));
        final var EP = input.paragraphs.get(Math.min(input.paragraphEndIndex,input.paragraphs.size()-1));
        for (int i = 0; i < input.document.getBodyElements().size(); i++) {
            final var element = input.document.getBodyElements().get(i);
            final String paragraphSString = element == SP ? "S*" : "  ";
            final String paragraphEString = element == EP ? "E*" : "  ";
            sb.append(paragraphSString).append(paragraphEString).append("(").append(i).append(":");
            if (element instanceof XWPFParagraph) {
                final XWPFParagraph paragraph = (XWPFParagraph) element;
                for (int j = 0; j < paragraph.getRuns().size(); j++) {
                    final String runSString = input.runStartIndex == j && element == SP ? "S*" : "";
                    final String runEString = input.runEndIndex == j && element == EP ? "E*" : "";
                    sb.append(runSString).append(runEString).append("[").append(j).append(":");
                    sb.append(paragraph.getRuns().get(j).getText(0));
                    sb.append("]");
                }
                if( input.runStartIndex >= paragraph.getRuns().size() && (element == SP || element == EP)) {
                    sb.append("... [S").append(input.runStartIndex).append(":]");
                }
                if( input.runEndIndex >= paragraph.getRuns().size() && (element == SP || element == EP)) {
                    sb.append("... [E").append(input.runEndIndex).append(":]");
                }
            } else if (element instanceof XWPFTable) {
                final XWPFTable table = (XWPFTable) element;
                sb.append("[").append(table.getRows().size()).append(",").append(table.getRows().get(0).getTableCells().size()).append("]");
                for (int j = 0; j < table.getRows().size(); j++) {
                    final var row = table.getRows().get(j);
                    sb.append("|| ");
                    for (int k = 0; k < row.getTableCells().size(); k++) {
                        final var cell = row.getTableCells().get(k);
                        final String cellSString = cell.getText();
                        sb.append(cellSString).append("|");
                    }
                    sb.append("|");
                    if( j < table.getRows().size() - 1 ) {
                        sb.append("\n");
                    }
                }

            }
            sb.append(")\n");
        }
//        System.out.println(prefix + sb);
    }

    public static void debugDoc(final String prefix, final XWPFDocument input, List<XWPFParagraph> paragraphs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.getParagraphs().size(); i++) {
            sb.append(i).append(":");
            for (int j = 0; j < input.getParagraphs().get(i).getRuns().size(); j++) {
                sb.append("[").append(j).append(":");
                sb.append(input.getParagraphs().get(i).getRuns().get(j).getText(0));
                sb.append("]");
            }
            sb.append("\n");
        }
        sb.append("-----------------------------------------------------------------\n");
        for (int i = 0; i < paragraphs.size(); i++) {
            sb.append(i).append(":");
            for (int j = 0; j < paragraphs.get(i).getRuns().size(); j++) {
                sb.append("[").append(j).append(":");
                sb.append(paragraphs.get(i).getRuns().get(j).getText(0));
                sb.append("]");
            }
            sb.append("\n");
        }
        //System.out.println(prefix + sb);
    }
}
