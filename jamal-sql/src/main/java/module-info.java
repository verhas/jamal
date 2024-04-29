import javax0.jamal.api.Macro;
import javax0.jamal.sql.Connect;
import javax0.jamal.sql.Loop;
import javax0.jamal.sql.Select;

module jamal.sql {
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires java.sql;
    provides Macro with Connect, javax0.jamal.sql.SqlDriver, Select, Loop;
}