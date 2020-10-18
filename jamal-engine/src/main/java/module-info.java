module jamal.engine {
    requires jamal.api;
    requires jamal.tools;
    uses javax0.jamal.api.Macro;
    exports javax0.jamal.engine;
    exports javax0.jamal;
    requires jdk.jshell;
}