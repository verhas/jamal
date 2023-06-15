package javax0.jamal.io;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.io.File;

public class Mkdir implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var file = Utils.getFile();
        final var recursive = Utils.getRecursive();
        Scan.using(processor).from(this).firstLine().keys(file, recursive).parse(in);

        final var fileName = Utils.getFile(file, in);

        final boolean done;
        if (recursive.is()) {
            done = new File(fileName).mkdirs();
        } else {
            done = new File(fileName).mkdir();
        }
        BadSyntax.when(!done,  "Directory '%s' cannot be created", fileName);
        return "";
    }

    @Override
    public String getId() {
        return "io:mkdir";
    }
}
