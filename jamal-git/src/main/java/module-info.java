import javax0.jamal.api.Macro;
import javax0.jamal.git.Connect;
import javax0.jamal.git.Tag;

module jamal.git {
    requires jamal.api;
    requires jamal.tools;
    requires org.eclipse.jgit;
    exports javax0.jamal.git;
    provides Macro with Connect, Tag;
}