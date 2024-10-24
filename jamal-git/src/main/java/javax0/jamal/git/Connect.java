package javax0.jamal.git;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.StringParameter;
import org.eclipse.jgit.api.Git;

@Macro.Name("git")
public class Connect implements Macro, Scanner.WholeInput {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var id = scanner.str(null, "id").defaultValue("$git");
        final var location = scanner.file(null, "location");
        scanner.done();

        try {
            final var repo = Git.open(location.file());
            final var holder = new Repo(repo, id.get());
            processor.define(holder);
            processor.deferredClose(holder);
        } catch (Exception e) {
            throw new BadSyntax("Cannot open git repository at " + location.file().getAbsolutePath(), e);
        }
        return "";
    }

    static Git git(Processor processor, StringParameter id) throws BadSyntax {
        final var repoId = id.get() != null ? id.get() : "$git";

        return processor.getRegister().getUserDefined(repoId)
                .filter(Repo.class::isInstance)
                .map(Repo.class::cast)
                .orElseThrow(() -> new BadSyntax("The git repository '" + repoId + "' does not exist or not a repo"))
                .getObject();
    }

}
