package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Macro.Name;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static org.apache.poi.ss.usermodel.CellType.BLANK;

@Name("xls:find")
public class Find implements Macro, Scanner {

    public static final int ROW_MAXINDEX = 0x100000;
    public static final int COL_MAXINDEX = 0x4000;

    public enum What {
        empty, blank, string, number, integer, eval, regex
    }

    public enum Where {
        inRow, inCol, inColumn
    }

    private static boolean noNeedInput(What what) {
        return what == What.empty || what == What.blank;
    }

    private static class NumberVersion {
        final double dContent;
        final long lngContent;

        private NumberVersion(double dContent, long lngContent) {
            this.dContent = dContent;
            this.lngContent = lngContent;
        }
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // snippet find_parops
        final var cellDef = new ParopsCell(scanner);
        //{%@snip celldef_parops%}
        final var cell = scanner.str(null, "cell").optional();
        // * `cell` is the reference to the cell where the search starts.
        // This can be used instead of specifying the `row` and `col` parameters
        final var whatDef = scanner.enumeration(What.class).defaultValue(What.empty);
        // * You can specify what you are looking for.
        // The default is `empty` meaning you want to get the first empty cell.
        // The possible values are
        // ** `empty` to find the first empty cell
        // ** `blank` to find the first cell that is blank
        // ** `string` to find the first cell that contains the specified string
        // ** `number` to find the first cell that contains the specified number as a floating point number
        // ** `integer` to find the first cell that contains the specified number as an integer
        // ** `eval` to find the first cell that contains the specified macro evaluated to `true`.
        // ** `regex` to find the first cell that matches the specified regular expression.
        // If the parameter option `$` is specified, then the default is `eval` and the search is for the macro.
        final var where = scanner.enumeration(Where.class).defaultValue(Where.inRow);
        // * You can specify where you are looking for the cell.
        // The value can be
        // ** `inRow` to search in the row
        // ** `inCol` to search in the column
        final var reverse = scanner.bool(null, "reverse");
        // * `reverse` will, as the name implies reverse the search direction.
        // This means that the search starts from the cell specified and goes to the beginning of the row or column.
        final var eval = scanner.str(null, "$", "macro").optional();
        // * `$` or `macro` specify a name for a macro.
        // If it is specified, then the macro will search for the cell that contains the input evaluated to `true`.
        // The value of this parameter is the name of the macro that will hold the content of the cell during the evaluation.
        // This macro will be defined before the first cell is examined and redefined before each next cell is examined.
        // Note that the input of the macro `xls:find` will be evaluated many times until the cell is found.
        // Be careful not to have side effects in the input.
        // See the examples below.
        final var limitRowDef = scanner.number(null, "limitRow").optional();
        // * `limitRow` will limit the search in the rows.
        // The search will go up to this row.
        // For example, if the value is 10, then the search will go up to row 9.
        // The default value is `0x100000`, decimal 1,048,576 that is the limit in Excel 2007 and later, {%@def orZero=or zero when the search is reversed%}.
        final var limitColDef = scanner.number(null, "limitCol").optional();
        // * `limitCol` will limit the search in the columns.
        // The search will go up to this column.
        // For example, if the value is 10, then the search will go up to column 9 (`A` to `J`).
        // The default value is `0x4000`, decimal 16,384 that is the limit in Excel 2007 and later, {%orZero%}.
        final var orElse = scanner.str(null, "orElse").optional();
        // * `orElse` is the value that will be returned if the cell is not found.
        // If this parameter is not specified, then the macro will throw an exception if the cell is not found.
        // The value can be any string, there is no check that the value is a valid cell reference.
        // end snippet
        scanner.done();
        InputHandler.skipWhiteSpaces(in);
        final var contentOriginal = in.toString();
        BadSyntax.when(!contentOriginal.isEmpty() && !eval.isPresent() && noNeedInput(whatDef.get(What.class)), "The content is not empty and the search is for '" + whatDef.get(What.class) + "'");
        BadSyntax.when(whatDef.isPresent() && whatDef.get(What.class) != What.eval && eval.isPresent(), "When '$', 'macro' specified the search type must be 'eval' (default)");
        BadSyntax.when(limitColDef.isPresent() && (limitColDef.get() < 0 || limitColDef.get() > COL_MAXINDEX), "The limitCol should be between 0 and 1,048,576");
        BadSyntax.when(limitRowDef.isPresent() && (limitRowDef.get() < 0 || limitRowDef.get() > ROW_MAXINDEX), "The limitRow should be between 0 and 16,384");

        final var wb = cellDef.getWorkbook(processor);
        final var cellReference = cell.isPresent() ? cell.get() : "";
        final var startCell = cellDef.getCellReference(cellReference, this, wb);
        final var row = new AtomicInteger(startCell.getRow());
        final var col = new AtomicInteger(startCell.getCol());
        final var sheet = cellDef.getSheet(cellReference, this, wb);
        final Consumer<AtomicInteger> stepper = reverse.is() ? (AtomicInteger::decrementAndGet) : (AtomicInteger::incrementAndGet);
        final Runnable step = where.get(Where.class) == Where.inRow ? () -> stepper.accept(col) : () -> stepper.accept(row);
        var what = whatDef.get(What.class);
        final NumberVersion holder = getNumbers(what, contentOriginal);
        final var limitRow = limitRowDef.isPresent() ? limitRowDef.get() : (reverse.is() ? 0 : ROW_MAXINDEX);
        final var limitCol = limitColDef.isPresent() ? limitColDef.get() : (reverse.is() ? 0 : COL_MAXINDEX);
        final var pattern = what == What.regex ? Pattern.compile(contentOriginal) : null;

        while (true) {

            if (reverse.is() ? (row.get() <= limitRow || col.get() <= limitCol)
                    : (row.get() >= limitRow || col.get() >= limitCol)) {
                if (orElse.isPresent()) {
                    return orElse.get();
                }
                throw new BadSyntax("Cannot find the cell");
            }
            final var r = sheet.getRow(row.get());
            final var c = r == null ? null : r.getCell(col.get());
            final var cellContent = CellMacro.cellContent(c);
            final String content;
            if (eval.isPresent()) {
                final var m = processor.newUserDefinedMacro(eval.get(), cellContent);
                processor.define(m);
                content = processor.process(contentOriginal);
                if (!whatDef.isPresent()) {
                    what = What.eval;
                }
            } else {
                content = contentOriginal;
            }
            switch (what) {
                case regex:
                    if (cellContent != null) {
                        if (pattern.matcher(cellContent).matches()) {
                            return getCellReference(sheet, row, col);
                        }
                    }
                    break;
                case eval:
                    if (cellContent != null) {
                        if (Boolean.parseBoolean(content)) {
                            return getCellReference(sheet, row, col);
                        }
                    }
                    break;
                case string:
                    if (content.equals(cellContent)) {
                        return getCellReference(sheet, row, col);
                    }
                    break;
                case number:
                    if (cellContent != null) {
                        try {
                            final var d = Double.parseDouble(cellContent);
                            if (d == holder.dContent) {
                                return getCellReference(sheet, row, col);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    break;
                case integer:
                    if (cellContent != null) {
                        try {
                            double d = Double.parseDouble(cellContent);
                            if (d == Math.rint(d) && d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
                                final var lng = Math.round(d);
                                if (lng == holder.lngContent) {
                                    return getCellReference(sheet, row, col);
                                }
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    break;
                case empty:
                    if (c == null) {
                        return getCellReference(sheet, row, col);
                    }
                    break;
                case blank:
                    if (c == null || c.getCellType() == BLANK || CellMacro.cellContent(c).isBlank()) {
                        return getCellReference(sheet, row, col);
                    }
                    break;
            }
            step.run();
        }
    }

    private static NumberVersion getNumbers(What what, String content) throws BadSyntax {
        try {
            switch (what) {
                case number:
                    return new NumberVersion(Double.parseDouble(content), 0);
                case integer:
                    return new NumberVersion(0.0, Math.round(Double.parseDouble(content)));
                default:
                    return new NumberVersion(0.0, 0);
            }
        } catch (NumberFormatException e) {
            throw new BadSyntax("The content is not a number");
        }
    }

    private static String getCellReference(Sheet sheet, AtomicInteger row, AtomicInteger col) {
        return new CellReference(sheet.getSheetName(), row.get(), col.get(), false, false).formatAsString();
    }
}
