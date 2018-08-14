module core {
    exports javax0.jamal.builtins;
    provides javax0.jamal.api.Macro with javax0.jamal.builtins.Define;
    requires javax0.jamal.api;
    requires  javax0.jamal.tools;
}