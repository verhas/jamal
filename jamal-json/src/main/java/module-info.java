import javax0.jamal.api.Macro;
import javax0.jamal.json.Add;
import javax0.jamal.json.Define;
import javax0.jamal.json.Dump;
import javax0.jamal.json.Get;
import javax0.jamal.json.Output;
import javax0.jamal.json.Set;

module jamal.json {
    exports javax0.jamal.json;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires org.json;
    provides Macro with Define, Dump, Output, Get, Add, Set;
}