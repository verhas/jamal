package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.InputHandler;

import static javax0.jamal.tools.FileTools.absolute;
import static javax0.jamal.tools.FileTools.getFileContent;
import static javax0.jamal.tools.FileTools.writeFileContent;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;


public class FileMacros {

    /**
     * Read the content of a file and return the content of the file. This is not the same as {@code import} because the
     * content of the file is used verbatim.
     *
     * Since include implemented the option [verbatim] this macro is obsolete and deprecated. Will be deleted.
     */
    public static class Read implements Macro {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            skipWhiteSpaces(in);
            var reference = in.getReference();
            final String inputFileName = in.toString().trim();
            final String fileName = getAbsoluteFileName(in, reference, inputFileName);
            return getFileContent(fileName,processor);
        }
    }

    private static String getAbsoluteFileName(Input in, String reference, String inputFileName) throws BadSyntaxAt {
        final String fileName;
        if (FileTools.isAbsolute(inputFileName)) {
            fileName = inputFileName;
        } else {
            if (reference == null) {
                throw new BadSyntaxAt("Cannot use file macro in a file that has no reference", in.getPosition());
            }
            fileName = absolute(reference, inputFileName);
        }
        return fileName;
    }

    public static class Write implements Macro {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            var reference = in.getReference();
            String[] parts = InputHandler.getParts(in, 2);
            if (parts.length < 2) {
                throw new BadSyntaxAt("Write macro needs two arguments but got " + parts.length, in.getPosition());
            }
            var inputFileName = parts[0];
            var content = parts[1];
            final String fileName = getAbsoluteFileName(in, reference, inputFileName);
            writeFileContent(fileName, content,processor);
            return "";
        }
    }
}
