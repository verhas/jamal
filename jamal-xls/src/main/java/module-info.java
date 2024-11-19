import javax0.jamal.api.Macro;
import javax0.jamal.xls.CellConvert.ToCell;
import javax0.jamal.xls.CellConvert.ToCol;
import javax0.jamal.xls.CellConvert.ToRow;
import javax0.jamal.xls.CellConvert.ToSheet;
import javax0.jamal.xls.*;

module jamal.xls {
    exports javax0.jamal.xls;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires org.apache.poi.poi;
    provides Macro with
            Open,
            Close,
            CellMacro,
            Set,
            Delete,
            Unmerge,
            Merge,
            ToCell,
            ToCol,
            ToRow,
            ToSheet,
            Find,
            Range,
            Move;
}