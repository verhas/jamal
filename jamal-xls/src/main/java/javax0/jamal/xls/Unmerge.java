package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Macro.Name;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import javax0.jamal.xls.utils.WorkbookUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

@Name("xls:unmerge")
public class Unmerge implements Macro, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // snippet unmerge_parops
        final var cellDef = new ParopsCell(scanner);
        // {%@snip celldef_parops%}
        // end snippet
        scanner.done();
        InputHandler.skipWhiteSpaces(in);
        String ref = "";
        try {
            final var wb = WorkbookUtils.getReadOnly(cellDef.workbook.get(), processor);
            final Cell cell = cellDef.getCell(in, this, wb);
            BadSyntax.when(cell == null, "Cannot unmerge cell, not defined");
            ref = new CellReference(cell).formatAsString();
            unmerge(cell);
            return "";
        } catch (BadSyntax e) {
            throw e;
        } catch (Exception e) {
            throw BadSyntax.format(e, "Cannot unmerge the cell in XLS '%s' %s", cellDef.workbook.get(), ref);
        }
    }

    private void unmerge(final Cell cell) {
        final var sheet = cell.getSheet();
        final var cellRow = cell.getRowIndex();
        final var cellColumn = cell.getColumnIndex();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(cellRow, cellColumn)) {
                sheet.removeMergedRegion(i);
                break;
            }
        }
    }

}
