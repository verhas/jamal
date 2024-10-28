import javax0.jamal.api.Macro;
import javax0.jamal.git.Connect;
import javax0.jamal.git.Tag;
import javax0.jamal.git.Commit;
import javax0.jamal.git.Format;

module jamal.git {
    requires jamal.api;
    requires jamal.tools;
    requires org.eclipse.jgit;
    requires jamal.core;
    exports javax0.jamal.git;
    provides Macro with Connect, Tag, Commit, Format;
}