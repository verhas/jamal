package javax0.jamal.poi.word;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.IRunBody;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;

public class DocxTools {

    private final XWPFDocument document;

    public DocxTools(final XWPFDocument document) {
        this.document = document;
    }

    public int getParagraphIndexInDocument(final XWPFParagraph paragraph) {
        int i = 0;
        for (final var par : document.getBodyElements()) {
            if (par == paragraph) {
                break;
            }
            i++;
        }
        return i;
    }


    public void copyParagraph(final XWPFParagraph source, final XWPFParagraph target) {
        target.getCTP().set(source.getCTP().copy());
        target.getCTP().getRList().clear();
        for (final var sourceRun : source.getRuns()) {
            final var r = target.getCTP().addNewR();
            r.set(sourceRun.getCTR());
            final var targetRun = new XWPFRun(r, (IRunBody) target);
            target.addRun(targetRun);
            copyPictures(sourceRun, targetRun);
        }
    }

    public void copyPictures(final XWPFRun source, final XWPFRun target) {
        for (int i = 0; i < source.getEmbeddedPictures().size(); i++) {
            final var sourcePicture = source.getEmbeddedPictures().get(i);
            final var targetPicture = target.getEmbeddedPictures().get(i);
            final var pictureData = sourcePicture.getPictureData();
            final String pictureId;
            try {
                pictureId = document.addPictureData(pictureData.getData(), pictureData.getPictureType());
            } catch (InvalidFormatException e) {
                // must not happen as it is copied from another file
                continue;
            }
            targetPicture.getCTPicture().getBlipFill().getBlip().setEmbed(pictureId);
        }
    }

    public void copyTable(XWPFTable source, XWPFTable target) {
        copyTable(source, target, true);
    }

    public  void copyTable(XWPFTable source, XWPFTable target, final boolean deleteFirstRow) {
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
                        copyTable(table, targetTable, false);
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
}
