import javax0.jamal.api.Debugger;
import javax0.jamal.api.ResourceReader;
import javax0.jamal.tools.HttpsInput;
import javax0.jamal.tools.NullDebugger;
import javax0.jamal.tools.ResourceInput;

module jamal.tools {
    uses ResourceReader;
    requires jamal.api;
    exports javax0.jamal.tools;
    exports javax0.jamal.tools.param to jamal.prog;
    requires java.scripting;
    requires levenshtein;
    provides Debugger with NullDebugger;
    provides ResourceReader with ResourceInput, HttpsInput;
}