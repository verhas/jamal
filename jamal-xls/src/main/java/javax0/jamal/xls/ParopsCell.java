package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.ScannerTools;
import javax0.jamal.tools.param.IntegerParameter;
import javax0.jamal.tools.param.StringParameter;
import javax0.jamal.xls.utils.WorkSheetUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

/**
 * This class contains four parops that are used by any macros that need to refer to a cell in an Excel workbook.
 * The constructor gets the scanner object and reads the parameters from the scanner object.
 * The {@code getCell()} method is used to get the cell object from the workbook.
 */
class ParopsCell {

    final StringParameter workbook;
    final StringParameter sheet;
    final IntegerParameter rowDef;
    final IntegerParameter colDef;

    ParopsCell(Scanner.ScannerObject scanner) {
        // snippet celldef_parops
        //{%@comment needs an extra line here to be referenced by other snippets%}
        workbook = scanner.str(null, "workbook", "wb").defaultValue(Open.XLS_WORKBOOK);
        // * `workbook` (aliases are `wb`) is the name of the workbook that is used to refer to the workbook in the rest of the document.
        // This parop is optional and the default value is `+{%@snip DEFAULT_WORKBOOK%}+`.
        sheet = scanner.str("xls:sheet", "sheet").defaultValue("");
        // * `xls:sheet` (aliased as `sheet`) can identify the sheet that the cell read is in.
        // When the sheet is not defined in any way, then the first sheet is used.
        // The name `xls:sheet` can also be used as a macro, thus if you define a macro with this name, its value will be used as the name of the sheet.
        // This feature can be used to set the default sheet is nothing else is defined.
        // This parop is usually used together with `xls:row` and `xls:col` to define the location of the cell.
        // They can be used to set one as a macro and use the other as a parop iterating through the cells in the same row or column.
        // The usual way is specifying the cell in the macro input the usual `Sheet!A1` style.
        rowDef = scanner.number("xls:row", "row").defaultValue(0);
        // * `xls:row` (aliased as `row`) is the row number of the cell.
        // The indexing starts from 0.
        colDef = scanner.number("xls:col", "col").defaultValue(0);
        // * `xls:col` (aliased as `col`) is the column number of the cell.
        // The indexing starts from 0.
        // end snippet
    }

    /**
     * Get the cell from the workbook. The cell can be specified in the input or by the row and column numbers.
     * If the cell reference does not contain a sheet name, then the sheet name is taken from the {@code sheet} parameter.
     * If there is no sheet name in the {@code sheet} parameter then the first sheet is used.
     *
     * @param in the input
     * @param macro the macro calling this method, used for error reporting only
     * @param wb the opened workbook. This method needs an opened workbook to get the default sheet if not specified,
     *           and it has to be opened by the caller to keep SRP. Also the workbook can be opened for read and for
     *           update.
     * @return the cell, or {@code null} if there is no such cell
     * @throws BadSyntax if the row or column is defined in a parop when there is also a cell reference in the input
     */
    public Cell getCell(final Input in, Macro macro, Workbook wb)throws BadSyntax {
        if (in.length() > 0) {
            ScannerTools.badSyntax(macro).whenParameters(rowDef, colDef).anyPresent("When specifying cell reference you cannot use 'coL' or 'row'.");
            var cr = new CellReference(in.toString());
            if (cr.getSheetName() == null && !sheet.get().isEmpty()) {
                cr = new CellReference(sheet.get() + "!" + in);
            }
            final var s = (cr.getSheetName() == null ? wb.getSheetAt(0) : wb.getSheet(cr.getSheetName()));
            if (s == null) return null;
            final var r = s.getRow(cr.getRow());
            if (r == null) return null;
            return r.getCell(cr.getCol());
        } else {
            final var s = WorkSheetUtils.get(sheet.get(), wb);
            if (s == null) return null;
            final var r = s.getRow(rowDef.get());
            if (r == null) return null;
            return r.getCell(colDef.get());
        }
    }
}
