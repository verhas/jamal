module jamal.extensions {
    exports javax0.jamal.extensions;
    provides javax0.jamal.api.Macro with javax0.jamal.extensions.Use, javax0.jamal.extensions.For;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires java.scripting;
}