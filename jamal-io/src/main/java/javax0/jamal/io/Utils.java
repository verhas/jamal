package javax0.jamal.io;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.BooleanParameter;
import javax0.jamal.tools.param.StringParameter;

public class Utils {
    static StringParameter getFile(final Scanner.ScannerObject scanner) {
        return scanner.str("io:outputFile", "io:output", "output", "io:file", "file");
    }

    static BooleanParameter getRecursive(final Scanner.ScannerObject scanner) {
        return scanner.bool("io:recursive", "recursive");
    }

    static String getFile(StringParameter file, Input in) throws BadSyntax {
        final var reference = in.getReference();
        return FileTools.absolute(reference, file.get());
    }
}
