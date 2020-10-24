module jamal.engine {
    uses javax0.jamal.api.Macro;

    exports javax0.jamal.engine;
    exports javax0.jamal;

    requires jamal.api;
    requires jamal.tools;
    requires jdk.jshell;
}