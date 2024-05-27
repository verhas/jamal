package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Macro.Name;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.ScannerTools;
import javax0.jamal.tools.param.EnumerationParameter;
import javax0.jamal.tools.param.IntegerParameter;
import javax0.jamal.tools.param.StringParameter;
import javax0.jamal.xls.utils.WorkSheetUtils;
import javax0.jamal.xls.utils.WorkbookUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import java.util.Arrays;
import java.util.stream.Collectors;

@Name("xls:cell")
public class CellMacro implements Macro, Scanner {
    public enum As {
        // snippet cell_as_enum
        value,
        // specify you want the value of the cell.
        // This is the default if you do not specity any of these options.
        // The value is usually the same as the content.
        // The difference is in the case of formulas.
        // For formulas the value is the calculated value, the content is the formula itself.
        content,
        // specify this if you want the content of the cell.
        // See the details in the description of the `value` option.
        type,
        // specify this if you want the type of the cell.
        format,
        // specify this if you want the format of the cell.
        comment,
        // specify this if you want the comment of the cell.
        commentAuthor,
        // specify this if you want the author of the comment of the cell.
        style,
        // specify this if you want the style of the cell.
        hasComment,
        // specify this if you want to know if the cell has a comment.
        // The result is `true` or `false`.
        isString,
        // specify this if you want to know if the cell is a string.
        // The result is `true` or `false`.
        isNumeric,
        // specify this if you want to know if the cell is a number.
        // The result is `true` or `false`.
        isBoolean,
        // specify this if you want to know if the cell is a boolean.
        // The result is `true` or `false`.
        isFormula,
        // specify this if you want to know if the cell is a formula.
        // The result is `true` or `false`.
        isBlank,
        // specify this if you want to know if the cell is blank.
        // The result is `true` or `false`.
        isError,
        // specify this if you want to know if the cell is an error.
        // The result is `true` or `false`.
        isNull,
        // specify this if you want to know if the cell is null.
        // The result is `true` or `false`.
        // end snippet
    }

    public enum Style {
        // snippet cell_style_enum
        toString,
        // is the default value for style.
        // The result will contain all the style elements
        align,
        // the alignment of the cell.
        border,
        // the border of the cell.
        fill,
        // the fill of the cell.
        dataFormat,
        // the data format of the cell.
        hidden,
        // the hidden property of the cell.
        locked,
        // the locked property of the cell.
        rotation,
        // the rotation of the cell.
        shrinkToFit,
        // the shrink to fit property of the cell.
        verticalAlignment,
        // the vertical alignment of the cell.
        wrapText
        // the wrap text property of the cell.
        // end snippet
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var cellDef = new ParopsCell(scanner);
        // snippet cell_parops
        // {%@snip celldef_parops%}
        final var as = scanner.enumeration(As.class).defaultValue(As.value);
        // * there are several options to specify what the result of the macro should be.
        // {%@snip cell_as_enum%}
        final var style = scanner.enumeration(Style.class).defaultValue(Style.toString);
        // * When you specify `style` then the result is a string that contains the style of the cell.
        // You can also specify one of the following parops to get only one specific style property.
        // {%@snip cell_style_enum%}
        // end snippet
        scanner.done();

        InputHandler.skipWhiteSpaces(in);
        String ref = "";
        try {
            final var wb = WorkbookUtils.getReadOnly(cellDef.workbook.get(), processor);
            final Cell cell = cellDef.getCell(in.toString(), this, wb);
            if (cell == null) return blank(as);
            ref = new CellReference(cell).formatAsString();
            return cellToString(cell, as.get(As.class), style.get(Style.class), wb);
        } catch (BadSyntax e) {
            throw e;
        } catch (Exception e) {
            throw BadSyntax.format(e, "Cannot read the XLS '%s' %s", cellDef.workbook.get(), ref);
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

    private static String cellToString(final Cell cell, final As as, final Style style, final Workbook workbook) {
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

    private static String getStyle(final Cell cell, final Style style) {
        final var cellStyle = cell.getCellStyle();
        switch (style) {
            case toString:
                return Arrays.stream(Style.values())
                        .filter(s -> s != Style.toString)
                        .map(s -> s + "=" + getStyle(cell, s))
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

    static String cellContent(final Cell cell) {
        if( cell == null ) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                final var d = cell.getNumericCellValue();
                if ( d == Math.rint(d) && d < Long.MAX_VALUE && d > Long.MIN_VALUE ) {
                    return String.valueOf((long)d);
                }
                return String.valueOf(d);
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
        if( cellValue == null ) return "";
        switch (cellValue.getCellType()) {
            case STRING:
                return cellValue.getStringValue();
            case NUMERIC:
                final var d = cellValue.getNumberValue();
                if ( d == Math.rint(d) && d < Long.MAX_VALUE && d > Long.MIN_VALUE ) {
                    return String.valueOf((long)d);
                }
                return String.valueOf(d);
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
}
