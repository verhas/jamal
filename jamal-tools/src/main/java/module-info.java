import javax0.jamal.api.Debugger;
import javax0.jamal.tools.NullDebugger;

module jamal.tools {
    requires jamal.api;
    exports javax0.jamal.tools;
    exports javax0.jamal.tools.param to jamal.prog;
    requires java.scripting;
    requires levenshtein;
    provides Debugger with NullDebugger;
}