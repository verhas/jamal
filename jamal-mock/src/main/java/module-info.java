import javax0.jamal.api.Macro;

module jamal.mock {
    exports javax0.jamal.mock;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    provides Macro with Mock;
}