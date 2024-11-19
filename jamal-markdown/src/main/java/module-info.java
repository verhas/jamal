import javax0.jamal.api.Macro;

module jamal.markdown {
    exports javax0.jamal.markdown;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires flexmark.util.ast;
    requires flexmark;
    requires flexmark.util.data;
    provides Macro with Markdown;
}