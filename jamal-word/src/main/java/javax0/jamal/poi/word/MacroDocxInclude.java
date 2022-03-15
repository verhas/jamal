package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import org.apache.poi.xwpf.usermodel.IRunBody;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MacroDocxInclude implements Macro {


    private static class CallBack implements XWPFContext.DocxIntermediaryCallBack {
        List<XWPFParagraph> paragraphs;
        private int paragraphStartIndex;
        private int runStartIndex;
        private XWPFDocument document;

        private final File file;

        CallBack(final File file) {
            this.file = file;
        }

        @Override
        public void setDocument(XWPFDocument document) {
            this.document = document;
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
        public void process() throws BadSyntax {
            try {
                final var includedDocument = new XWPFDocument(new FileInputStream(file));
                int i = getParagraphIndexInDocument(paragraphs.get(paragraphStartIndex));
                final var newPars = new ArrayList<XWPFParagraph>();
                for (int j = 0; j < paragraphStartIndex; j++) {
                    newPars.add(paragraphs.get(j));
                }
                for (final var element : includedDocument.getBodyElements()) {
                    if (element instanceof XWPFParagraph) {
                        final var paragraph = (XWPFParagraph) element;
                        final var cursor = paragraphs.get(i).getCTP().newCursor();
                        final var p = document.insertNewParagraph(cursor);
                        newPars.add(p);
                        final CTP ctp = (CTP) paragraph.getCTP().copy();
                        p.getCTP().set(ctp);
                        p.getCTP().getRList().clear();
                        for (final var run : paragraph.getRuns()) {
                            final var r = p.getCTP().addNewR();
                            r.set(run.getCTR());
                            p.addRun(new XWPFRun(r, (IRunBody) p));
                        }
                        /*
                        for (final var run : paragraph.getRuns()) {
                            p.addRun(run);
                        }
                        */
                    } else if (element instanceof XWPFTable) {
                        final var table = (XWPFTable) element;
                    }
                }
                for (int j = paragraphStartIndex + 1; j < paragraphs.size(); j++) {
                    newPars.add(paragraphs.get(j));
                }
                paragraphs.clear();
                paragraphs.addAll(newPars);
            } catch (IOException e) {
                throw new BadSyntax(String.format("Cannot include the doc file '%s'", file.getAbsolutePath()), e);
            }
            DebugTool.debugDoc("doc:INCLUDE\n", document, paragraphs);
        }

        private int getParagraphIndexInDocument(final XWPFParagraph paragraph) {
            int i = 0;
            for (final var par : document.getBodyElements()) {
                if (par == paragraph) {
                    break;
                }
                i++;
            }
            return i;
        }
    }

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var position = in.getPosition();
        var reference = position.file;
        var fileName = FileTools.absolute(reference, in.toString().trim());
        final var context = XWPFContext.getXWPFContext(processor);
        context.register(new CallBack(new File(fileName)));
        return "";
    }

    @Override
    public String getId() {
        return "docx:include";
    }
}
