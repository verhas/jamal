package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
import org.apache.poi.ss.util.CellReference;

import java.util.function.Function;

public class CellConvert {

    private static String to(final String cell, final Function<CellReference, String> converter) throws BadSyntax {
        try {
            final var cr = new CellReference(cell);
            final var s = converter.apply(cr);
            if( s == null ){
                throw new BadSyntax("Cannot convert the cell reference");
            }
            return s;
        } catch (Exception e) {
            throw new BadSyntax("Cannot convert the cell reference", e);
        }

    }

    @Macro.Name("xls:row")
    public static class ToRow implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws javax0.jamal.api.BadSyntax {
            return to(in.toString().trim(), cr -> "" + cr.getRow());
        }
    }

    @Macro.Name("xls:col")
    public static class ToCol implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws javax0.jamal.api.BadSyntax {
            return to(in.toString().trim(), cr -> "" + cr.getCol());
        }
    }

    @Macro.Name("xls:sheet")
    public static class ToSheet implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws javax0.jamal.api.BadSyntax {
            return to(in.toString().trim(), CellReference::getSheetName);
        }
    }

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
            if( sheet.isPresent()){
                cr = new CellReference(sheet.get(), rowDef.get(), colDef.get(),rowAbsolute.is(), colAbsolute.is());
            } else {
                cr = new CellReference(rowDef.get(), colDef.get(),rowAbsolute.is(), colAbsolute.is());
            }
            return cr.formatAsString();
        }
    }

}
