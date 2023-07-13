module jamal.maven {
    requires jamal.api;
    requires jamal.engine;
    requires jamal.tools;
    requires maven.plugin.api;
    requires maven.plugin.annotations;
    requires java.xml;
    requires org.slf4j;
    uses javax0.jamal.api.Macro;
}