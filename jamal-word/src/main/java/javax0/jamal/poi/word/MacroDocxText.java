package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.util.List;

@Macro.Name("docx:text")
public
class MacroDocxText implements Macro {


    private static class CallBack implements XWPFContext.DocxIntermediaryCallBack {
        List<XWPFParagraph> paragraphs;
        private int paragraphStartIndex;
        private int runStartIndex;


        private final String text;

        CallBack(final String text) {
            this.text = text;
        }

        @Override
        public void setParagraphs(final List<XWPFParagraph> paragraphs) {
            this.paragraphs = paragraphs;
        }

        @Override
        public void setParagraphStartIndex(int paragraphStartIndex) {
            this.paragraphStartIndex = paragraphStartIndex;
        }

        @Override
        public void setRunStartIndex(int runStartIndex) {
            this.runStartIndex = runStartIndex;
        }

        @Override
        public void process() {
            paragraphs.get(paragraphStartIndex).insertNewRun(runStartIndex).setText(text);
        }
    }

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var context = XWPFContext.getXWPFContext(processor);
        context.register(new CallBack(in.toString().repeat(2)));
        return "";
    }

}
