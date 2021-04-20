import javax0.jamal.api.Macro;
import javax0.jamal.doclet.Code;

module jamal.doclet {
    requires jdk.javadoc;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires jamal.core;
    requires jamal.snippet;
    exports javax0.jamal.doclet;
    provides Macro with Code;
}