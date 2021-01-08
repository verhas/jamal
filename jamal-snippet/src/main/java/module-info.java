import javax0.jamal.api.Macro;
import javax0.jamal.snippet.Clear;
import javax0.jamal.snippet.Collect;
import javax0.jamal.snippet.Number;
import javax0.jamal.snippet.Snippet;
import javax0.jamal.snippet.Trim;

module jamal.snippet {
    exports javax0.jamal.snippet;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires java.scripting;
    provides Macro with Clear,
        Collect,
        Number,
        Snippet,
        Trim
        ;
}