import javax0.jamal.api.Macro;
import javax0.jamal.sql.*;

module jamal.sql {
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires java.sql;
    requires net.sf.jsqlparser;
    provides Macro with Connect, Select, Loop, Statement, Close;
}