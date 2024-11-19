import javax0.jamal.api.Macro;

module jamal.rest {
    exports javax0.jamal.rest;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires jdk.httpserver;
    provides Macro with Rest;
}