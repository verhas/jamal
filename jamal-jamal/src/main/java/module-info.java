import javax0.jamal.api.Macro;
import javax0.jamal.jamal.Output;
import javax0.jamal.jamal.Sample;

module jamal.jamal {
    exports javax0.jamal.jamal;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    provides Macro with Output, Sample;
}