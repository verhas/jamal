module jamal.core {
    exports javax0.jamal.builtins;
    provides javax0.jamal.api.Macro with
        javax0.jamal.builtins.Define,
        javax0.jamal.builtins.Comment,
        javax0.jamal.builtins.Block,
        javax0.jamal.builtins.Import,
        javax0.jamal.builtins.Include,
        javax0.jamal.builtins.Env,
        javax0.jamal.builtins.Export,
        javax0.jamal.builtins.Sep,
        javax0.jamal.builtins.Script,
        javax0.jamal.builtins.Eval,
        javax0.jamal.builtins.Begin,
        javax0.jamal.builtins.End,
        javax0.jamal.builtins.For,
        javax0.jamal.builtins.If,
        javax0.jamal.builtins.Use,
        javax0.jamal.builtins.Options,
        javax0.jamal.builtins.Ident,
        javax0.jamal.builtins.Try,
        javax0.jamal.builtins.JShell,
        javax0.jamal.builtins.Escape,
        javax0.jamal.builtins.Require,
        javax0.jamal.builtins.Undefine,
        javax0.jamal.builtins.Defer,
        javax0.jamal.builtins.Macro,
        javax0.jamal.builtins.Debug

        ;

    requires jamal.api;
    requires jamal.tools;
    requires java.scripting;
    requires jdk.jshell;
}