import javax0.jamal.api.Debugger;
import javax0.jamal.api.Macro;
import javax0.jamal.engine.NullDebugger;

module jamal.engine {
    uses Macro;
    uses Debugger;

    exports javax0.jamal.engine;
    exports javax0.jamal;

    requires jamal.api;
    requires jamal.tools;
    requires jdk.jshell;
    requires levenshtein;
    provides Debugger with NullDebugger;
}