package javax0.jamal.xls.utils;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.xls.Open;
import org.apache.poi.ss.usermodel.Workbook;

public class WorkbookUtils {

    public static Workbook getReadOnly(final String id, final Processor processor) throws BadSyntax {
        return get(id, processor, true);
    }
    public static Workbook get(final String id, final Processor processor) throws BadSyntax {
        return get(id, processor, false);
    }
    public static Workbook get(final String id, final Processor processor, boolean readOnly) throws BadSyntax {
        final var workbookHolder = processor.getRegister().getUserDefined(id);
        BadSyntax.when(workbookHolder.isEmpty(), "The workbook '%s' does not exist", id);
        BadSyntax.when(!(workbookHolder.get() instanceof Open.WorkbookHolder), "The workbook '%s' is not a workbook", id);
        BadSyntax.when( ((Open.WorkbookHolder) workbookHolder.get()).getObject().isClosed(), "The workbook '%s' is closed", id);
        final var wbh = ((Open.WorkbookHolder) workbookHolder.get()).getObject();
        if( wbh.readOnly && !readOnly ){
            BadSyntax.format("The workbook '%s' is read only", id);
        }
        return wbh.workbook;
    }
}
