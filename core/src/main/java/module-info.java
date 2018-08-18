module jamal.core {
    exports javax0.jamal.builtins;
    provides javax0.jamal.api.Macro with javax0.jamal.builtins.Define,
        javax0.jamal.builtins.Comment,
        javax0.jamal.builtins.Import,
        javax0.jamal.builtins.Include
        ;
    requires jamal.api;
    requires jamal.tools;
}