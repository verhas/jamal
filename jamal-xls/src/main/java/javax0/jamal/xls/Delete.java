package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.IntegerParameter;
import javax0.jamal.tools.param.StringParameter;
import javax0.jamal.xls.utils.WorkbookUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import static javax0.jamal.xls.Set.Name;

@Name("xls:delete")
public class Delete implements Macro, Scanner.WholeInput {

    public enum What {
        SHEET, ROW, COL, COLUMN, CELL
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var cellDef = new ParopsCell(scanner);
        // snippet delete_parops
        //{%@snip celldef_parops%}
        final var cellRef = scanner.str(null, "cell").optional();
        // * `cell` is the cell reference that is used to refer to the cell in the rest of the document.
        final var whatDef = scanner.enumeration(What.class).optional();
        // * `SHEET`, `ROW`, `COL`, `COLUMN`, `CELL` is the type of the object that is to be deleted.
        // end snippet
        scanner.done();
        final What what;
        if (whatDef.isPresent()) {
            what = whatDef.get(What.class);
        } else {
            if (cellDef.sheet.isPresent() && !cellDef.rowDef.isPresent() && !cellDef.colDef.isPresent() && !cellRef.isPresent()) {
                what = What.SHEET;
            } else if (cellDef.rowDef.isPresent() && !cellDef.colDef.isPresent() && !cellRef.isPresent()) {
                what = What.ROW;
            } else if (cellDef.colDef.isPresent() && !cellDef.rowDef.isPresent() && !cellRef.isPresent()) {
                what = What.COLUMN;
            } else if (cellRef.isPresent() && !cellDef.rowDef.isPresent() && !cellDef.colDef.isPresent()) {
                what = What.CELL;
            } else {
                throw new BadSyntax("Cannot decide what to delete");
            }
        }

        final var wb = WorkbookUtils.get(cellDef.workbook.get(), processor);
        switch (what) {
            case SHEET:
                BadSyntax.when(cellDef.colDef.isPresent() || cellDef.rowDef.isPresent(), "Should not specify row or column when deleting a sheet");
                deleteSheet(wb, cellDef.sheet, cellRef);
                return "";
            case ROW:
                BadSyntax.when(cellDef.colDef.isPresent(), "Should not specify column when deleting a row");
                BadSyntax.when(cellDef.rowDef.isPresent() && cellRef.isPresent(), "Should not specify both row and cell when deleting a row");
                deleteRow(wb, cellDef.sheet, cellDef.rowDef, cellRef);
                return "";
            case COLUMN:
            case COL:
                BadSyntax.when(cellDef.rowDef.isPresent(), "Should not specify row when deleting a column");
                BadSyntax.when(cellDef.colDef.isPresent() && cellRef.isPresent(), "Should not specify column and cell when deleting a column");
                deleteCol(wb, cellDef.sheet, cellDef.colDef, cellRef);
                return "";
            case CELL:
                BadSyntax.when(cellDef.colDef.isPresent() || cellDef.rowDef.isPresent(), "Should not specify row or column when deleting a cell");
                BadSyntax.when(!cellRef.isPresent(), "Should specify cell when deleting a cell");
                deleteCell(wb, cellDef.sheet, cellRef);
                return "";
        }
        return "";
    }

    private static void deleteCol(final Workbook wb,
                                  final StringParameter sheetDef,
                                  final IntegerParameter colDef,
                                  final StringParameter cellDef) throws BadSyntax {
        final Sheet sheet;
        final int col;
        try {
            if (cellDef.isPresent()) {
                final var cr = getCellReference(sheetDef, cellDef);
                if (cr.getSheetName() == null) {
                    sheet = wb.getSheetAt(0);
                } else {
                    sheet = wb.getSheet(cr.getSheetName());
                }
                col = cr.getCol();
            } else {
                if (sheetDef.isPresent()) {
                    sheet = wb.getSheet(sheetDef.get());
                } else {
                    sheet = wb.getSheetAt(0);
                }
                col = colDef.get();
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
                                  final IntegerParameter rowDef,
                                  final StringParameter cellDef) throws BadSyntax {
        final Sheet sheet;
        final Row rowToDelete;
        try {
            if (cellDef.isPresent()) {
                var cr = getCellReference(sheetDef, cellDef);
                if (cr.getSheetName() == null) {
                    if (sheetDef.get().isEmpty()) {
                        cr = new CellReference(wb.getSheetAt(0).getSheetName() + "!" + cellDef.get());
                    } else {
                        cr = new CellReference(sheetDef.get() + "!" + cellDef.get());
                    }
                }
                sheet = wb.getSheet(cr.getSheetName());
                rowToDelete = sheet.getRow(cr.getRow());
            } else {
                if (sheetDef.isPresent()) {
                    sheet = wb.getSheet(sheetDef.get());
                } else {
                    sheet = wb.getSheetAt(0);
                }
                rowToDelete = sheet.getRow(rowDef.get());
            }
            if (rowToDelete != null) {
                final var rowToDeleteIndex = rowToDelete.getRowNum();
                sheet.removeRow(rowToDelete);
                if (rowToDeleteIndex < sheet.getLastRowNum()) {
                    sheet.shiftRows(rowToDeleteIndex + 1, sheet.getLastRowNum(), -1);
                }
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
}
