import javax0.jamal.api.Macro;
import javax0.jamal.xls.*;

module jamal.xls {
    exports javax0.jamal.xls;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires org.apache.poi.poi;
    provides Macro with Open, Close, CellMacro, Set, Delete;
}