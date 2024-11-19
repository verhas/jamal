import javax0.jamal.api.Macro;

module jamal.json {
    exports javax0.jamal.json;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires org.json;
    provides Macro with Define,  Get, Set, Length, Keys;
}