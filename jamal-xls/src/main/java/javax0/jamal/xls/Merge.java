package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.param.StringParameter;
import javax0.jamal.xls.utils.WorkbookUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import java.lang.reflect.Parameter;

import static javax0.jamal.xls.Set.*;

@Name("xls:merge")
public class Merge implements Macro, WholeInput {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // snippet merge_parops
        final var workbook = scanner.str(null, "workbook", "wb").defaultValue(Open.XLS_WORKBOOK);
        // * `workbook` (aliases are `wb`) is the name of the workbook that is used to refer to the workbook in the
        //    rest of the document.
        //    This parop is optional and the default value is `+{%@snip DEFAULT_WORKBOOK%}+`.
        final var sheetDef = scanner.str(XLS_SHEET, "sheet").defaultValue("");
        // * `sheet` is the name of the sheet where the region is merged.
        final var topDef = scanner.number(XLS_ROW, "top").optional();
        // * `top` is the top row of the region to merge.
        final var leftDef = scanner.number(XLS_COL, "left").optional();
        // * `left` is the left column of the region to merge.
        final var bottomDef = scanner.number(null, "bottom").optional();
        // * `bottom` is the bottom row of the region to merge.
        final var rightDef = scanner.number(null, "right").optional();
        // * `right` is the right column of the region to merge.
        final var regionDef = scanner.str(null, "region").optional();
        // * `region` is the region to merge in the format `A1:B2`.
        // end snippet
        scanner.done();

        BadSyntax.when(regionDef.isPresent() && (topDef.isPresent() || leftDef.isPresent() || bottomDef.isPresent() || rightDef.isPresent()),
                "Cannot specify region and top, left, bottom or right at the same time.");
        BadSyntax.when(!regionDef.isPresent() && !(topDef.isPresent() && leftDef.isPresent() && bottomDef.isPresent() && rightDef.isPresent()),
                "Should specify region or sheet, top, left, bottom and right at the same time.");
        final var wb = WorkbookUtils.get(workbook.get(), processor);

        if (regionDef.isPresent()) {
            final var parts = regionDef.get().split(":");
            BadSyntax.when(parts.length != 2, "Invalid region definition");
            final var topLeft = getCellReference(wb, sheetDef.get(), parts[0]);
            final var bottomRight = getCellReference(wb, topLeft.getSheetName(), parts[1]);
            BadSyntax.when(!topLeft.getSheetName().equals(bottomRight.getSheetName()),
                    "Region should be in the same sheet");
            mergeRegion(wb, topLeft, bottomRight);
        } else {
            final var top = topDef.get();
            final var left = leftDef.get();
            final var bottom = bottomDef.get();
            final var right = rightDef.get();
            mergeRegion(wb, sheetDef.get(), top, left, bottom, right);
        }
        return "";
    }

    private void mergeRegion(Workbook wb, String sheetName, int top, int left, int bottom, int right) {
        final var sheet = sheetName != null && !sheetName.isBlank() ? wb.getSheet(sheetName) : wb.getSheetAt(0);
        sheet.addMergedRegion(new CellRangeAddress(top, bottom, left, right));
    }

    private void mergeRegion(Workbook wb, CellReference topLeft, CellReference bottomRight) {
        final var sheet = wb.getSheet(topLeft.getSheetName());
        sheet.addMergedRegion(new CellRangeAddress(
                topLeft.getRow(),
                bottomRight.getRow(),
                topLeft.getCol(),
                bottomRight.getCol()));
    }

    private static CellReference getCellReference(Workbook wb, String sheet, String cellCoordinate) throws BadSyntax {
        var cr = new CellReference(cellCoordinate);
        if (cr.getSheetName() == null) {
            if (!sheet.isBlank()) {
                return new CellReference(sheet + "!" + cellCoordinate);
            } else {
                return new CellReference(wb.getSheetAt(0).getSheetName() + "!" + cellCoordinate);
            }
        } else {
            return cr;
        }
    }
}
