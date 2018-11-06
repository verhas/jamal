module jamal.core {
    exports javax0.jamal.extensions;
    provides javax0.jamal.api.Macro with
        javax0.jamal.extensions.Use;
    requires jamal.api;
    requires jamal.tools;
    requires java.scripting;
}