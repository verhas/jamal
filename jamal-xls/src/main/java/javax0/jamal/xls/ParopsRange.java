package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.IntegerParameter;
import javax0.jamal.tools.param.StringParameter;
import javax0.jamal.xls.utils.WorkbookUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import static javax0.jamal.xls.Set.*;

/**
 * This class contains the parameter options to handle a region. This is used by the macro merge and region.
 * <p>
 * The class has a method `init` that initializes the region. it has to be invoked after the parameter scannign is done
 * and before the querying of the parameters.
 * <p>
 * A range is defined by six parameters:
 *
 * <ol>
 *     <li>Workbook</li>
 *     <li>Sheet in the workbook</li>
 *     <li>Top row</li>
 *     <li>Left column</li>
 *     <li>Bottom row</li>
 *     <li>Right column</li>
 * </ol>
 * <p>
 * These parameters can be queried after the init() method as
 *
 * <pre>
 *     var range = new ParopsRange(scanner,processor);
 *     ...
 *     scanner.done()
 *     ...
 *     {@link #init range.init()};
 *     {@link #top() range.top()};
 *     {@link #left range.left()};
 *     {@link #bottom range.bottom()};
 *     {@link #right range.right()};
 *     {@link #sheet range.sheet()};
 *     {@link #workbook range.workbook()};
 *
 * </pre>
 */
class ParopsRange {

    final StringParameter workbook;
    final StringParameter sheetDef;
    final IntegerParameter topDef;
    final IntegerParameter leftDef;
    final IntegerParameter bottomDef;
    final IntegerParameter rightDef;
    final StringParameter regionDef;
    final private Processor processor;

    ParopsRange(Scanner.ScannerObject scanner, Processor processor) {
        this.processor = processor;
        // snippet rangedef_parops
        //{%@comment needs an extra line here to be referenced by other snippets%}
        workbook = scanner.str(null, "workbook", "wb").defaultValue(Open.XLS_WORKBOOK);
        // * `workbook` (aliases are `wb`) is the name of the workbook that is used to refer to the workbook in the
        //    rest of the document.
        //    This parop is optional and the default value is `+{%@snip DEFAULT_WORKBOOK%}+`.
        sheetDef = scanner.str(XLS_SHEET, "sheet").defaultValue("");
        // * `sheet` is the name of the sheet where the region is merged.
        // If a sheet is defined, it has to be already in the workbook.
        // If there is no sheet defined anywhere, either as this option or in the `region` then the first sheet is used.
        topDef = scanner.number(XLS_ROW, "top").optional();
        // * `top` is the top row of the region to merge. {%@def ZI=Numbering is zero indexed.%}
        leftDef = scanner.number(XLS_COL, "left").optional();
        // * `left` is the left column of the region to merge. {%ZI%}
        bottomDef = scanner.number(null, "bottom").optional();
        // * `bottom` is the bottom row of the region to merge. {%ZI%}
        rightDef = scanner.number(null, "right").optional();
        // * `right` is the right column of the region to merge. {%ZI%}
        regionDef = scanner.str(null, "region").optional();
        // * `region` is the region to merge in the format `A1:B2`.
        // The definition may contain the name of the sheet, but the sheet name in the first and second part of the region should be the same.
        // It is enough to define the sheet in the first part, but not only in the second part.
        // If the sheet name is defined as `sheet` and also here, the two definitions should be identical.
        // end snippet
    }

    private boolean initDone = false;
    private Workbook wb;
    private Sheet sheet;

    /**
     * Get the sheet the region is in.
     *
     * @return the sheet object
     */
    public Sheet sheet() {
        return sheet;
    }

    /**
     * Get the workbook the region is in.
     *
     * @return the workbook object
     */
    public Workbook workbook() {
        return wb;
    }


    private class Corners {
        int top;
        int left;
        int bottom;
        int right;

        void split(boolean readOnly) throws BadSyntax {
            if (!initDone) {
                initDone = true;
                wb = WorkbookUtils.get(workbook.get(), processor, readOnly);
                String[] cr = regionDef.get().split(":");
                BadSyntax.when(cr.length != 2, "Invalid region definition");
                final var ctl = getCellReference(sheetDef.get(), cr[0]);
                final var cbr = getCellReference(ctl.getSheetName(), cr[1]);
                BadSyntax.when(!ctl.getSheetName().equals(cbr.getSheetName()), "Region should be in the same sheet");
                sheet = wb.getSheet(ctl.getSheetName());
                top = ctl.getRow();
                left = ctl.getCol();
                bottom = cbr.getRow();
                right = cbr.getCol();
            }
        }

        public void fill() throws BadSyntax {
            if (!initDone) {
                initDone = true;
                wb = WorkbookUtils.get(workbook.get(), processor);
                if (sheetDef.isPresent()) {
                    sheet = wb.getSheet(sheetDef.get());
                } else {
                    sheet = wb.getSheetAt(0);
                }
                corners.top = topDef.get();
                corners.left = leftDef.get();
                corners.bottom = bottomDef.get();
                corners.right = rightDef.get();
            }
        }

        private CellReference getCellReference(String sheet, String cellCoordinate) throws BadSyntax {
            var cr = new CellReference(cellCoordinate);
            if (cr.getSheetName() == null) {
                if (sheet.isBlank()) {
                    BadSyntax.when(wb.getNumberOfSheets() == 0 ,"There are no sheets in the workbook.");
                    return new CellReference(wb.getSheetAt(0).getSheetName() + "!" + cellCoordinate);
                } else {
                    return new CellReference(sheet + "!" + cellCoordinate);
                }
            } else {
                BadSyntax.when(!sheet.isBlank() && !sheet.equals(cr.getSheetName()), "There is a sheet defined and it different from the sheet in the cell reference. '%s' != '%s'", sheet, cr.getSheetName());
                return cr;
            }
        }

    }

    final Corners corners = new Corners();

    /**
     * Initializes the structure after the scanning of the parameters were done.
     * <p>
     * This method calls {@link #init(boolean) init(false)}
     */
    void init() throws BadSyntax {
        init(false);
    }

    /**
     * Initializes the structure after the scanning of the parameters were done.
     *
     * @param readOnly if the workbook is read-only
     * @throws BadSyntax if the range was badly defined, there is some inconsistency in the range definitions -
     *                   for example, one corner of the range is on a different sheet than the other corner.
     */
    void init(final boolean readOnly) throws BadSyntax {
        if (regionDef.isPresent()) {
            corners.split(readOnly);
        } else {
            corners.fill();
        }
        assertions();
    }

    /**
     * Get the top row of the region.
     *
     * @return the top row of the region. The numbering is zero indexed.
     */
    public int top() {
        return corners.top;
    }

    /**
     * Get the left column of the region.
     *
     * @return the left column of the region. The numbering is zero indexed.
     */
    public int left() {
        return corners.left;
    }

    /**
     * Get the bottom row of the region.
     *
     * @return the bottom row of the region. The numbering is zero indexed.
     */
    public int bottom() {
        return corners.bottom;
    }

    /**
     * Get the right column of the region.
     *
     * @return the right column of the region. The numbering is zero indexed.
     */
    public int right() {
        return corners.right;
    }

    private void assertions() throws BadSyntax {
        BadSyntax.when(regionDef.isPresent() && (topDef.isPresent() || leftDef.isPresent() || bottomDef.isPresent() || rightDef.isPresent()),
                "Cannot specify region and top, left, bottom or right at the same time.");
        BadSyntax.when(!regionDef.isPresent() && !(topDef.isPresent() && leftDef.isPresent() && bottomDef.isPresent() && rightDef.isPresent()),
                "Should specify region or sheet, top, left, bottom and right at the same time.");
    }

}
