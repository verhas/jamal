package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import static javax0.jamal.xls.Set.Name;
import static javax0.jamal.xls.Set.WholeInput;

@Name("xls:merge")
public class Merge implements Macro, WholeInput {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // snippet merge_parops
        final var range = new ParopsRange(scanner,processor);
        // {%@snip rangedef_parops%}
        // end snippet
        scanner.done();

        range.init();
        final var wb = range.workbook();
        try {
            mergeRegion(wb, range.sheet(), range.top(), range.left(), range.bottom(), range.right());
        }catch(Exception e){
            throw new BadSyntax("Cannot merge region", e);
        }
        return "";
    }

    private void mergeRegion(Workbook wb, Sheet sheet, int top, int left, int bottom, int right) {
        sheet.addMergedRegion(new CellRangeAddress(top, bottom, left, right));
    }

}
