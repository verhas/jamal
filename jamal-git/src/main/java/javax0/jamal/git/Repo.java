package javax0.jamal.git;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.tools.IdentifiedObjectHolder;
import org.eclipse.jgit.api.Git;

import java.io.Closeable;

public class Repo extends IdentifiedObjectHolder<Git> implements UserDefinedMacro , Closeable {

    public Repo(Git git, String id) {
        super(git, id);
    }

    @Override
    public String evaluate(String... parameters) throws BadSyntax {
        return "";
    }

    @Override
    public void close() {
        getObject().close();
    }
}
