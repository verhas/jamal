package javax0.jamal.io;

import javax0.jamal.api.*;
import javax0.jamal.tools.Params;

import java.io.File;

public class IoFile implements Macro, InnerScopeDependent {

    private enum Condition {
        exists, isDirectory, isFile, canExecute, canRead, canWrite, isHidden
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var file = Params.holder("io:file", "file").asString();
        final var condition = Params.holder(null, "isHidden", "exists", "isDirectory", "isFile", "canExecute", "canRead", "canWrite").asBoolean();
        Params.using(processor).from(this).tillEnd().keys(file, condition).parse(in);


        final var fileName = Utils.getFile(file, in);
        final var f = new File(fileName);
        final Condition cond = condition.is() ? Condition.valueOf(condition.name()) : Condition.exists;

        switch (cond) {
            case exists:
                return Boolean.toString(f.exists());
            case isDirectory:
                return Boolean.toString(f.isDirectory());
            case isFile:
                return Boolean.toString(f.isFile());
            case canExecute:
                return Boolean.toString(f.canExecute());
            case canRead:
                return Boolean.toString(f.canRead());
            case canWrite:
                return Boolean.toString(f.canWrite());
            case isHidden:
                return Boolean.toString(f.isHidden());
            default:
                throw new BadSyntax("Unknown condition '" + cond + "'");
        }
    }

    @Override
    public String getId() {
        return "io:file";
    }
}
