import javax0.jamal.api.Debugger;
import javax0.jamal.api.Macro;

module jamal.engine {
    uses Macro;
    uses Debugger;

    exports javax0.jamal.engine;
    exports javax0.jamal;
    exports javax0.jamal.engine.macro;
    exports javax0.jamal.engine.util;

    requires jamal.api;
    requires jamal.tools;
    requires jdk.jshell;
    requires levenshtein;
    requires java.scripting;
    provides javax.script.ScriptEngineFactory with javax0.jamal.engine.JamalEngineFactory;
}