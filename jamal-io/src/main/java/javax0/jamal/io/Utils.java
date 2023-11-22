package javax0.jamal.io;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.BooleanParameter;
import javax0.jamal.tools.param.StringParameter;

/**
 * A few little utility functions used by different macros.
 */
class Utils {

    /**
     * Create and return a {@link StringParameter} that can be used to get the output file name from the input.
     *
     * @param scanner the scanner to use to get the parameter
     * @return the parameter
     */
    static StringParameter getFile(final Scanner.ScannerObject scanner) {
        return scanner.str("io:outputFile", "io:output", "output", "io:file", "file");
    }

    /**
     * Create and return a {@link BooleanParameter} that can be used to get the recursive flag from the input.
     *
     * @param scanner the scanner to use to get the parameter
     * @return the parameter
     */
    static BooleanParameter getRecursive(final Scanner.ScannerObject scanner) {
        return scanner.bool("io:recursive", "recursive");
    }

    /**
     * Get the absolute file name from the parameter {@code file} and the reference {@code in}.
     *
     * @param file the parameter that contains the file name
     * @param in the input that is used as a reference
     * @return the absolute file name
     * @throws BadSyntax if the file name is not a valid file name
     */
    static String getFile(StringParameter file, Input in) throws BadSyntax {
        final var reference = in.getReference();
        return FileTools.absolute(reference, file.get());
    }
}
