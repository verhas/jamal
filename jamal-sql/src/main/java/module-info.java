import javax0.jamal.api.Macro;
import javax0.jamal.sql.Connect;
import javax0.jamal.sql.Loop;
import javax0.jamal.sql.Select;
import javax0.jamal.sql.Statement;

module jamal.sql {
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires java.sql;
    requires net.sf.jsqlparser;
    provides Macro with Connect, Select, Loop, Statement;
}