package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.ScannerTools;
import javax0.jamal.tools.param.EnumerationParameter;
import javax0.jamal.xls.utils.WorkSheetUtils;
import javax0.jamal.xls.utils.WorkbookUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CellMacro implements Macro, Scanner {
    public enum As {
        value, content, type, format, comment, commentAuthor, style,
        hasComment,
        isString,
        isNumeric,
        isBoolean,
        isFormula,
        isBlank,
        isError,
        isNull,
    }

    public enum Style {
        toString, align, border, fill, dataFormat, hidden, locked, rotation, shrinkToFit, verticalAlignment, wrapText

    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var workbook = scanner.str(null, "workbook", "wb").defaultValue(Open.XLS_WORKBOOK);
        final var sheet = scanner.str("xls:sheet", "sheet").defaultValue("");
        final var row = scanner.str("xls:row", "row").defaultValue("0");
        final var col = scanner.str("xls:col", "col").defaultValue("0");
        final var as = scanner.enumeration(As.class).defaultValue(As.value);
        final var style = scanner.enumeration(Style.class).defaultValue(Style.toString);
        scanner.done();

        InputHandler.skipWhiteSpaces(in);
        String ref = "";
        try {
            final var wb = WorkbookUtils.get(workbook.get(), processor);
            final Cell cell;
            if (in.length() > 0) {
                ScannerTools.badSyntax(this).whenParameters(row, col).anyPresent("When specifying cell reference you cannot use 'coL' or 'row'.");
                var cr = new CellReference(in.toString());
                if (cr.getSheetName() == null && !sheet.get().isEmpty()) {
                    cr = new CellReference(sheet.get() + "!" + in);
                }
                final var s = (cr.getSheetName() == null ? wb.getSheetAt(0) : wb.getSheet(cr.getSheetName()));
                if (s == null) return blank(as);
                final var r = s.getRow(cr.getRow());
                if (r == null) return blank(as);
                cell = r.getCell(cr.getCol());
                ref = cr.formatAsString();
            } else {
                final var s = WorkSheetUtils.get(sheet.get(), wb);
                if (s == null) return blank(as);
                final var r = s.getRow(Integer.parseInt(row.get()));
                if (r == null) return blank(as);
                cell = r.getCell(Integer.parseInt(col.get()));
                if (cell == null) return blank(as);
                ref = new CellReference(cell).formatAsString();
            }
            if (cell == null) return blank(as);
            return cellToString(cell, as.get(As.class), style.get(Style.class), wb);
        } catch (Exception e) {
            throw BadSyntax.format(e, "Cannot read the XLS '%s' %s", workbook.get(), ref);
        }
    }

    private String blank(EnumerationParameter as) throws BadSyntax {
        switch (as.get(As.class)) {
            case isString:
            case isNumeric:
            case isBoolean:
            case isFormula:
            case isError:
            case hasComment:
                return "false";
            case isBlank:
            case isNull:
                return "true";
            default:// every other case like value, content, format etc...
                return "";
        }
    }

    private static String cellToString(final org.apache.poi.ss.usermodel.Cell cell, final As as, final Style style, final Workbook workbook) {
        switch (as) {
            case isString:
                return "" + (cell.getCellType() == CellType.STRING);
            case isNumeric:
                return "" + (cell.getCellType() == CellType.NUMERIC);
            case isBoolean:
                return "" + (cell.getCellType() == CellType.BOOLEAN);
            case isFormula:
                return "" + (cell.getCellType() == CellType.FORMULA);
            case isBlank:
                return "" + (cell.getCellType() == CellType.BLANK);
            case isError:
                return "" + (cell.getCellType() == CellType.ERROR);
            case isNull:
                return "" + false;
            case hasComment:
                return "" + (cell.getCellComment() != null);
            case comment:
                return cell.getCellComment() == null ? "" : cell.getCellComment().getString().getString();
            case commentAuthor:
                return cell.getCellComment() == null ? "" : cell.getCellComment().getAuthor();
            case style:
                return getStyle(cell, style);
            case value:
                final var evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                final var cellValue = evaluator.evaluate(cell);
                return cellContent(cellValue);
            case content:
                return cellContent(cell);
            case type:
                return cell.getCellType().name();
            case format:
                return cell.getCellStyle().getDataFormatString();
            default:
                throw new IllegalArgumentException("Unknown as type: " + as);
        }
    }

    private static String getStyle(final org.apache.poi.ss.usermodel.Cell cell, final Style style) {
        final var cellStyle = cell.getCellStyle();
        switch (style) {
            case toString:
                return Arrays.stream(Style.values())
                        .filter(s -> s != Style.toString)
                        .map( s -> s + "=" + getStyle(cell, s))
                        .collect(Collectors.joining(", "));
            case align:
                return cellStyle.getAlignment().name();
            case border:
                return cellStyle.getBorderBottom().name();
            case fill:
                return cellStyle.getFillPattern().name();
            case dataFormat:
                return cellStyle.getDataFormatString();
            case hidden:
                return "" + cellStyle.getHidden();
            case locked:
                return "" + cellStyle.getLocked();
            case rotation:
                return "" + cellStyle.getRotation();
            case shrinkToFit:
                return "" + cellStyle.getShrinkToFit();
            case verticalAlignment:
                return cellStyle.getVerticalAlignment().name();
            case wrapText:
                return "" + cellStyle.getWrapText();
            default:
                throw new IllegalArgumentException("Unknown style type: " + style);
        }
    }

    private static String cellContent(final org.apache.poi.ss.usermodel.Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            case ERROR:
                return "ERROR#" + cell.getErrorCellValue();
            default:
                throw new IllegalArgumentException("Unknown cell type: " + cell.getCellType());
        }
    }

    private static String cellContent(final CellValue cellValue) {
        switch (cellValue.getCellType()) {
            case STRING:
                return cellValue.getStringValue();
            case NUMERIC:
                return String.valueOf(cellValue.getNumberValue());
            case BOOLEAN:
                return String.valueOf(cellValue.getBooleanValue());
            case BLANK:
                return "";
            case ERROR:
                return "ERROR#" + cellValue.getErrorValue();
            default:
                throw new IllegalArgumentException("Unknown cell type: " + cellValue.getCellType());
        }
    }

    @Override
    public String getId() {
        return "xls:cell";
    }
}
