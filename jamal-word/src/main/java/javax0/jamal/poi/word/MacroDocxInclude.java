package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.IRunBody;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MacroDocxInclude implements Macro {


    private static class CallBack implements XWPFContext.DocxIntermediaryCallBack {
        List<XWPFParagraph> paragraphs;
        private int paragraphStartIndex;
        private int runStartIndex;
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
                boolean noTable = true;
                for (final var element : includedDocument.getBodyElements()) {
                    if (element instanceof XWPFParagraph) {
                        final var paragraph = (XWPFParagraph) element;
                        final var cursor = paragraphs.get(i).getCTP().newCursor();
                        final var p = document.insertNewParagraph(cursor);
                        if (noTable) {
                            newPars.add(p);
                        }
                        copyParagraph(paragraph, p);
                    } else if (element instanceof XWPFTable) {
                        final var table = (XWPFTable) element;
                        final var cursor = paragraphs.get(i).getCTP().newCursor();
                        final var t = document.insertNewTbl(cursor);
                        final var pos = document.getTablePos(document.getPosOfTable(t));
                        if (noTable) {
                            positionSetter.accept(t);
                            noTable = false;
                        }
                        copyTable(table, t);
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

        private void copyParagraph(final XWPFParagraph source, final XWPFParagraph target) {
            target.getCTP().set(source.getCTP().copy());
            target.getCTP().getRList().clear();
            for (final var run : source.getRuns()) {
                final var r = target.getCTP().addNewR();
                r.set(run.getCTR());
                target.addRun(new XWPFRun(r, (IRunBody) target));
            }
        }

        private void copyParagraph1(XWPFParagraph source, XWPFParagraph target) {
            target.getCTP().setPPr(source.getCTP().getPPr());
            for (int i = 0; i < source.getRuns().size(); i++) {
                XWPFRun run = source.getRuns().get(i);
                XWPFRun targetRun = target.createRun();
                //copy formatting
                targetRun.getCTR().setRPr(run.getCTR().getRPr());
                //no images just copy text
                targetRun.setText(run.getText(0));
            }
        }

        private void copyTable(XWPFTable source, XWPFTable target) {
            copyTable(source, target, true);
        }
        private void copyTable(XWPFTable source, XWPFTable target, final boolean deleteFirstRow) {
            target.getCTTbl().setTblPr(source.getCTTbl().getTblPr());
            target.getCTTbl().setTblGrid(source.getCTTbl().getTblGrid());
            for (int r = 0; r < source.getRows().size(); r++) {
                XWPFTableRow targetRow = target.createRow();
                XWPFTableRow sourceRow = source.getRows().get(r);
                targetRow.getCtRow().setTrPr(sourceRow.getCtRow().getTrPr());
                for (int c = 0; c < sourceRow.getTableCells().size(); c++) {
                    //newly created row may have 1 cell
                    XWPFTableCell targetCell = c < targetRow.getTableCells().size() ? targetRow.getTableCells().get(c) : targetRow.createCell();
                    XWPFTableCell cell = sourceRow.getTableCells().get(c);
                    targetCell.getCTTc().setTcPr(cell.getCTTc().getTcPr());
                    XmlCursor cursor = targetCell.getParagraphArray(0).getCTP().newCursor();
                    for (int p = 0; p < cell.getBodyElements().size(); p++) {
                        IBodyElement elem = cell.getBodyElements().get(p);
                        if (elem instanceof XWPFParagraph) {
                            XWPFParagraph targetPar = targetCell.insertNewParagraph(cursor);
                            cursor.toNextToken();
                            XWPFParagraph par = (XWPFParagraph) elem;
                            copyParagraph(par, targetPar);
                        } else if (elem instanceof XWPFTable) {
                            XWPFTable targetTable = targetCell.insertNewTbl(cursor);
                            cursor.toNextToken();
                            XWPFTable table = (XWPFTable) elem;
                            copyTable(table, targetTable,false);
                        }
                    }
                    //newly created cell has one default paragraph we need to remove
                    targetCell.removeParagraph(targetCell.getParagraphs().size() - 1);
                }
            }
            //newly created table can have one row by default. we need to remove the default row.
            if (deleteFirstRow) {
                target.removeRow(0);
            }
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
