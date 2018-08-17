module jamal.core {
    exports javax0.jamal.builtins;
    provides javax0.jamal.api.Macro with javax0.jamal.builtins.Define;
    requires jamal.api;
    requires  jamal.tools;
}