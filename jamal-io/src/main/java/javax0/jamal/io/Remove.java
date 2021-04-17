package javax0.jamal.io;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class Remove implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var file = Utils.getFile();
        final var recursive = Utils.getRecursive();
        Params.using(processor).keys(file, recursive).parse(in);

        final var fileName = Utils.getFile(file, in);

        try {
            final var allDeleted = new AtomicBoolean(true);
            if (recursive.is()) {
                Files.walk(Paths.get(fileName))
                    .map(Path::toFile)
                    .sorted((o1, o2) -> -o1.compareTo(o2))
                    .forEach(f -> allDeleted.set(allDeleted.get() && f.delete()));
                if (!allDeleted.get()) {
                    throw new BadSyntax("Not possible to delete the file/dir and all files/dirs under '"
                        + fileName + "'");
                }
            } else {
                Files.delete(Paths.get(fileName));
            }
        } catch (IOException ioException) {
            throw new BadSyntax("Not possible to delete '" + fileName + "'", ioException);
        }
        return "";
    }

    @Override
    public String getId() {
        return "io:remove";
    }
}
