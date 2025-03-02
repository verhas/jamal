package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Macro.Name("docx:include")
public
class MacroDocxInclude implements Macro {


    private static class CallBack implements XWPFContext.DocxIntermediaryCallBack {
        private List<XWPFParagraph> paragraphs;
        private int paragraphStartIndex;
        private XWPFDocument document;
        private Consumer<IBodyElement> positionSetter;
        private final File file;

        CallBack(final File file) {
            this.file = file;
        }

        @Override
        public void setPosition(final Consumer<IBodyElement> positionSetter) {
            this.positionSetter = positionSetter;
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
        public void process() throws BadSyntax {
            try {
                final var tools = new DocxTools(document);
                final var includedDocument = new XWPFDocument(new FileInputStream(file));
                final var newPars = new ArrayList<XWPFParagraph>();
                for (int j = 0; j < paragraphStartIndex; j++) {
                    newPars.add(paragraphs.get(j));
                }
                boolean noTable = true;
                for (final var element : includedDocument.getBodyElements()) {
                    final var cursor = paragraphs.get(paragraphStartIndex).getCTP().newCursor();
                    if (element instanceof XWPFParagraph) {
                        final var paragraph = (XWPFParagraph) element;
                        final var p = document.insertNewParagraph(cursor);
                        if (noTable) {
                            newPars.add(p);
                        }
                        tools.copyParagraph(paragraph, p);
                    } else if (element instanceof XWPFTable) {
                        final var table = (XWPFTable) element;
                        final var t = document.insertNewTbl(cursor);
                        if (noTable) {
                            positionSetter.accept(t);
                            noTable = false;
                        }
                        tools.copyTable(table, t);
                    }
                }
                if (noTable) {
                    for (int j = paragraphStartIndex + 1; j < paragraphs.size(); j++) {
                        newPars.add(paragraphs.get(j));
                    }
                }
                paragraphs.clear();
                paragraphs.addAll(newPars);
            } catch (IOException e) {
                throw new BadSyntax(String.format("Cannot include the doc file '%s'", file.getAbsolutePath()), e);
            }
            DebugTool.debugDoc("doc:INCLUDE\n", document, paragraphs);
        }


    }

    /*
     *
     * tag::include[]
     * {%macro docx::include%}
     *
     * Using this macro you can include the formatted content of another docx file into the currently processed one.
     * The syntax of the macro is
     *
     * [source]
     * ----
     *   {@docx:include file_name}
     * ----
     * The file name can be absolute or relative to the processed file.
     * The macro will copy the content of the included file into the current file.
     * After that the copied parts will be processed by Jamal the same way as they had been in the document before.
     * This means that including a file from an already included file should use a file name relative to the top level document and not the included one.
     *
     * end::include[]
     */
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var position = in.getPosition();
        var reference = position.file;
        var fileName = FileTools.absolute(reference, in.toString().trim());
        final var context = XWPFContext.getXWPFContext(processor);
        context.register(new CallBack(new File(fileName)));
        return "";
    }

}
