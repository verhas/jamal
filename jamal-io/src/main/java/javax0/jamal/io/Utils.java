package javax0.jamal.io;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;

public class Utils {
    static Params.Param<String> getFile() {
        return Params.holder("io:outputFile", "io:output", "output", "io:file", "file").asString();
    }

    static Params.Param<Boolean> getRecursive() {
        return Params.holder("io:recursive", "recursive").asBoolean();
    }

    static String getFile(Params.Param<String> file, Input in) throws BadSyntax {
        final var reference = in.getReference();
        return FileTools.absolute(reference, file.get());
    }
}
