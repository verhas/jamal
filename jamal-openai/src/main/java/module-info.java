import javax0.jamal.api.Macro;

module jamal.openai {
    exports javax0.jamal.openai;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires org.json;
    provides Macro with LowLevelApi.Get, LowLevelApi.Post;
}