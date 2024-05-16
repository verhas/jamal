package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.StringParameter;
import javax0.jamal.xls.utils.WorkbookUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import static javax0.jamal.xls.Set.*;

public class Delete implements Macro, Scanner.WholeInput {

    public enum What {
        SHEET, ROW, COL, COLUMN, CELL
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var workbook = scanner.str(null, "workbook", "wb").defaultValue(Open.XLS_WORKBOOK);
        final var sheetDef = scanner.str(XLS_SHEET, "sheet").defaultValue("");
        final var rowDef = scanner.str(XLS_ROW, "row").optional();
        final var colDef = scanner.str(XLS_COL, "col").optional();
        final var cellDef = scanner.str(null, "cell").optional();
        final var whatDef = scanner.enumeration(What.class).optional();
        scanner.done();
        final What what;
        if (whatDef.isPresent()) {
            what = whatDef.get(What.class);
        } else {
            if (sheetDef.isPresent() && !rowDef.isPresent() && !colDef.isPresent() && !cellDef.isPresent()) {
                what = What.SHEET;
            } else if (rowDef.isPresent() && !colDef.isPresent() && !cellDef.isPresent()) {
                what = What.ROW;
            } else if (colDef.isPresent() && !rowDef.isPresent() && !cellDef.isPresent()) {
                what = What.COLUMN;
            } else if (cellDef.isPresent() && !rowDef.isPresent() && !colDef.isPresent()) {
                what = What.CELL;
            } else {
                throw new BadSyntax("Cannot decide what to delete");
            }
        }

        final var wb = WorkbookUtils.get(workbook.get(), processor);
        switch (what) {
            case SHEET:
                BadSyntax.when(colDef.isPresent() || rowDef.isPresent(), "Should not specify row or column when deleting a sheet");
                deleteSheet(wb, sheetDef, cellDef);
                return "";
            case ROW:
                BadSyntax.when(colDef.isPresent(), "Should not specify column when deleting a row");
                BadSyntax.when(rowDef.isPresent() && cellDef.isPresent(), "Should not specify both row and cell when deleting a row");
                deleteRow(wb, sheetDef, rowDef, cellDef);
                return "";
            case COLUMN:
            case COL:
                BadSyntax.when(rowDef.isPresent(), "Should not specify row when deleting a column");
                BadSyntax.when(colDef.isPresent() && cellDef.isPresent(), "Should not specify column and cell when deleting a column");
                deleteCol(wb, sheetDef, colDef, cellDef);
                return "";
            case CELL:
                BadSyntax.when(colDef.isPresent() || rowDef.isPresent(), "Should not specify row or column when deleting a cell");
                BadSyntax.when(!cellDef.isPresent(), "Should specify cell when deleting a cell");
                deleteCell(wb, sheetDef, cellDef);
                return "";
        }
        return "";
    }

    private static void deleteCol(final Workbook wb,
                                  final StringParameter sheetDef,
                                  final StringParameter colDef,
                                  final StringParameter cellDef) throws BadSyntax {
        final Sheet sheet;
        final int col;
        try {
            if (cellDef.isPresent()) {
                final var cr = getCellReference(sheetDef, cellDef);
                sheet = wb.getSheet(cr.getSheetName());
                col = cr.getCol();
            } else {
                if (sheetDef.isPresent()) {
                    sheet = wb.getSheet(sheetDef.get());
                } else {
                    sheet = wb.getSheetAt(0);
                }
                col = Integer.parseInt(colDef.get());
            }
            deleteColumn(sheet, col);
        } catch (Exception e) {
            throw new BadSyntax("Cannot delete column", e);
        }
    }

    public static void deleteColumn(Sheet sheet, int colToDelete) {
        for (Row row : sheet) {
            for (int c = colToDelete; c < row.getLastCellNum(); c++) {
                final var oldCell = row.createCell(c);
                if (c + 1 < row.getLastCellNum()) { // Check if there is a next cell
                    final var nextCell = row.getCell(c + 1);
                    if (nextCell != null) {
                        copyCell(nextCell, oldCell);
                    }
                }

            }
            // Remove the last cell which is now duplicated
            if (row.getLastCellNum() > colToDelete) {
                Cell lastCell = row.getCell(row.getLastCellNum() - 1);
                if (lastCell != null) {
                    row.removeCell(lastCell);
                }
            }
        }
    }

    public static void copyCell(Cell oldCell, Cell newCell) {
        newCell.setCellStyle(oldCell.getCellStyle());

        switch (oldCell.getCellType()) {
            case STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            case BLANK:
                newCell.setBlank();
                break;
            case ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            default:
                break;
        }
    }

    private static void deleteCell(final Workbook wb,
                                   final StringParameter sheetDef,
                                   final StringParameter cellDef) throws BadSyntax {
        try {
            final var cr = getCellReference(sheetDef, cellDef);
            final var row = wb.getSheet(cr.getSheetName()).getRow(cr.getRow());
            row.removeCell(row.getCell(cr.getCol()));
        } catch (Exception e) {
            throw new BadSyntax("Cannot delete cell", e);
        }
    }

    private static void deleteRow(final Workbook wb,
                                  final StringParameter sheetDef,
                                  final StringParameter rowDef,
                                  final StringParameter cellDef) throws BadSyntax {
        final Sheet sheet;
        final Row rowToDelete;
        try {
            if (cellDef.isPresent()) {
                final var cr = getCellReference(sheetDef, cellDef);
                sheet = wb.getSheet(cr.getSheetName());
                rowToDelete = sheet.getRow(cr.getRow());
            } else {
                if (sheetDef.isPresent()) {
                    sheet = wb.getSheet(sheetDef.get());
                } else {
                    sheet = wb.getSheetAt(0);
                }
                rowToDelete = sheet.getRow(Integer.parseInt(rowDef.get()));
            }
            final var rowToDeleteIndex = rowToDelete.getRowNum();
            sheet.removeRow(rowToDelete);
            if (rowToDeleteIndex != sheet.getLastRowNum()) {
                sheet.shiftRows(rowToDeleteIndex + 1, sheet.getLastRowNum(), -1);
            }
        } catch (Exception e) {
            throw new BadSyntax("Cannot delete row", e);
        }
    }

    private static void deleteSheet(final Workbook wb,
                                    final StringParameter sheet,
                                    final StringParameter cellDef) throws BadSyntax {
        final String sheetName = getSheetName(sheet, cellDef);
        try {
            if (sheetName.matches("\\d+")) {
                wb.removeSheetAt(Integer.parseInt(sheetName));
            } else {
                final var index = wb.getSheetIndex(sheetName);
                BadSyntax.when(index == -1, "Sheet '" + sheetName + "' does not exist");
                wb.removeSheetAt(index);
            }
        } catch (Exception e) {
            throw new BadSyntax("Cannot delete sheet '" + sheetName + "'", e);
        }
    }

    private static String getSheetName(StringParameter sheet, StringParameter cellDef) throws BadSyntax {
        final String sheetName;
        if (cellDef.isPresent()) {
            var cr = getCellReference(sheet, cellDef);
            sheetName = cr.getSheetName();
        } else {
            sheetName = sheet.get();
        }
        return sheetName;
    }

    private static CellReference getCellReference(StringParameter sheet, StringParameter cellDef) throws BadSyntax {
        var cr = new CellReference(cellDef.get());
        if (cr.getSheetName() == null && !sheet.get().isEmpty()) {
            return new CellReference(sheet.get() + "!" + cellDef.get());
        } else {
            return cr;
        }
    }

    @Override
    public String getId() {
        return "xls:delete";
    }
}
