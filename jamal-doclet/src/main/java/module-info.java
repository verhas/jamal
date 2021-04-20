import javax0.jamal.api.Macro;
import javax0.jamal.doclet.Code;
import javax0.jamal.doclet.Link;

module jamal.doclet {
    requires jdk.javadoc;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires jamal.core;
    exports javax0.jamal.doclet;
    provides Macro with Code, Link;
}