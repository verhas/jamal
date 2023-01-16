import javax0.jamal.api.Macro;
import javax0.jamal.yaml.Add;
import javax0.jamal.yaml.Define;
import javax0.jamal.yaml.Dump;
import javax0.jamal.yaml.Format;
import javax0.jamal.yaml.Get;
import javax0.jamal.yaml.IsResolved;
import javax0.jamal.yaml.Output;
import javax0.jamal.yaml.Ref;
import javax0.jamal.yaml.Resolve;
import javax0.jamal.yaml.Set;
import javax0.jamal.yaml.Xml;
import javax0.jamal.yaml.YamlString;

module jamal.yaml {
    exports javax0.jamal.yaml;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires org.yaml.snakeyaml;
    requires ognl;
    provides Macro with Define, Resolve, Ref, Dump, Output, Get, Xml, Add, IsResolved, Format, YamlString,Set;
}