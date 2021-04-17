import javax0.jamal.api.Macro;
import javax0.jamal.yaml.Define;
import javax0.jamal.yaml.Dump;
import javax0.jamal.yaml.Get;
import javax0.jamal.yaml.Output;
import javax0.jamal.yaml.Ref;
import javax0.jamal.yaml.Resolve;

module jamal.yaml {
    exports javax0.jamal.yaml;
    requires jamal.api;
    requires jamal.tools;
    requires snakeyaml;
    requires ognl;
    provides Macro with Define, Resolve, Ref, Dump, Output, Get;
}