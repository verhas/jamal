package javax0.jamal.poi.word;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.util.List;

public class DebugTool {
    private static final Logger LOGGER = LogManager.getLogger(DebugTool.class);

    public static void debugDoc(final String prefix, final XWPFInput input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.paragraphs.size(); i++) {
            final String paragraphSString = input.paragraphStartIndex == i ? "S*" : "  ";
            final String paragraphEString = input.paragraphEndIndex == i ? "E*" : "  ";
            sb.append(paragraphSString).append(paragraphEString).append("(").append(i).append(":");
            for (int j = 0; j < input.paragraphs.get(i).getRuns().size(); j++) {
                final String runSString = input.runStartIndex == j && input.paragraphStartIndex == i ? "S*" : "";
                final String runEString = input.runEndIndex == j && input.paragraphEndIndex == i ? "E*" : "";
                sb.append(runSString).append(runEString).append("[").append(j).append(":");
                sb.append(input.paragraphs.get(i).getRuns().get(j).getText(0));
                sb.append("]");
            }
            sb.append(")\n");
        }
        //System.out.println(prefix + sb);
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
