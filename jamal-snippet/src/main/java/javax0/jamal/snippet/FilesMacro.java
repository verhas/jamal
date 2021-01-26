package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.MacroReader;
import javax0.jamal.tools.PlaceHolders;

import java.io.File;

/**
 * Inner classes implement macros that ease the handling of document references to files and directories.
 */
public class FilesMacro {

    /**
     * Check that the directory exists and that it is a directory.
     */
    public static class Directory implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var reader = MacroReader.macro(processor);
            final var format = reader.readValue("directoryFormat").orElse("$name");
            final var root = reader.readValue("root").orElse("");
            final var dirName = FileTools.absolute(in.getReference(), root + in.toString().trim());
            final var dir = new File(dirName);
            if (!dir.exists()) {
                throw new BadSyntaxAt("The directory '" + dirName + "' does not exist.", in.getPosition());
            }
            if (!dir.isDirectory()) {
                throw new BadSyntaxAt("The directory '" + dirName + "' exists but it is not a directory.", in.getPosition());
            }

            try {
                return PlaceHolders.of(
                    "$name", dirName,
                    "$absolutePath", dir.getAbsolutePath(),
                    "$parent", dir.getParent()
                ).and(
                    "$canonicalPath", () -> dir.getCanonicalPath()
                ).format(format);
            } catch (Exception e) {
                throw new BadSyntaxAt("Directory name '" + dirName
                    + "'cannot be formatted using the given format '"
                    + format + "'", in.getPosition(), e);
            }
        }

        @Override
        public String getId() {
            return "directory";
        }
    }

}
