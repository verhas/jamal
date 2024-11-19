import javax0.jamal.api.Macro;

module jamal.yaml {
    exports javax0.jamal.yaml;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires org.yaml.snakeyaml;
    requires ognl;
    provides Macro with Define, Resolve, Ref, Dump, Output, Get, Xml, Add, IsResolved, Format, YamlString,Set;
}