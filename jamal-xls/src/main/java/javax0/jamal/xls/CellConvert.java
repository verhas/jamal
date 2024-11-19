package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
import org.apache.poi.ss.util.CellReference;

import java.util.function.Function;

/**
 * Utility class providing macros to convert cell references in Excel files
 * using Apache POI. Each inner class represents a specific macro for
 * extracting or formatting cell information (e.g., row, column, sheet).
 */
public class CellConvert {

    /**
     * Converts the given cell reference string using the provided converter function.
     *
     * @param cell      the cell reference string (e.g., "A1", "Sheet1!B2").
     * @param converter a function that transforms a {@link CellReference} into a
     *                  string.
     * @return the converted string based on the cell reference.
     * @throws BadSyntax if the cell reference cannot be converted.
     */
    private static String to(final String cell, final Function<CellReference, String> converter) throws BadSyntax {
        try {
            final var cr = new CellReference(cell);
            final var s = converter.apply(cr);
            if (s == null) {
                throw new BadSyntax("Cannot convert the cell reference");
            }
            return s;
        } catch (Exception e) {
            throw new BadSyntax("Cannot convert the cell reference", e);
        }
    }

    /**
     * Macro class to retrieve the row number from an Excel cell reference.
     * For example, given "A1", it returns "0" (zero-based row index).
     */
    @Macro.Name("xls:row")
    public static class ToRow implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws javax0.jamal.api.BadSyntax {
            return to(in.toString().trim(), cr -> "" + cr.getRow());
        }
    }

    /**
     * Macro class to retrieve the column index from an Excel cell reference.
     * For example, given "B2", it returns "1" (zero-based column index).
     */
    @Macro.Name("xls:col")
    public static class ToCol implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws javax0.jamal.api.BadSyntax {
            return to(in.toString().trim(), cr -> "" + cr.getCol());
        }
    }

    /**
     * Macro class to retrieve the sheet name from an Excel cell reference.
     * If the reference does not contain a sheet name, returns {@code null}.
     * For example, given "Sheet1!C3", it returns "Sheet1".
     */
    @Macro.Name("xls:sheet")
    public static class ToSheet implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws javax0.jamal.api.BadSyntax {
            return to(in.toString().trim(), CellReference::getSheetName);
        }
    }

    /**
     * Macro class to construct a cell reference string given row, column, and
     * optional sheet name, along with options for absolute or relative referencing.
     * <p>
     * This macro builds a fully formatted cell reference from its components.
     */
    @Macro.Name("xls:to:cell")
    public static class ToCell implements Macro, Scanner.WholeInput {
        @Override
        public String evaluate(Input in, Processor processor) throws javax0.jamal.api.BadSyntax {
            final var scanner = newScanner(in, processor);
            final var rowDef = scanner.number(null, "row").required();
            final var rowAbsolute = scanner.bool(null, "rowAbsolute");
            final var colDef = scanner.number(null, "col").required();
            final var colAbsolute = scanner.bool(null, "colAbsolute");
            final var sheet = scanner.str(null, "sheet").optional();
            scanner.done();
            final CellReference cr;
            if (sheet.isPresent()) {
                cr = new CellReference(sheet.get(), rowDef.get(), colDef.get(), rowAbsolute.is(), colAbsolute.is());
            } else {
                cr = new CellReference(rowDef.get(), colDef.get(), rowAbsolute.is(), colAbsolute.is());
            }
            return cr.formatAsString();
        }
    }

}
