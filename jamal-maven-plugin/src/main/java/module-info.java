module jamal.maven {
    requires jamal.api;
    requires jamal.engine;
    requires jamal.tools;
    requires jamal.extensions;
    requires maven.plugin.api;
    requires maven.plugin.annotations;
    uses javax0.jamal.api.Macro;
}