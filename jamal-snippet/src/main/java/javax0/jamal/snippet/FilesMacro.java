package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.IndexedPlaceHolders;
import javax0.jamal.tools.Scan;

import java.io.File;
import java.nio.file.Paths;

import static javax0.jamal.tools.IndexedPlaceHolders.value;
import static javax0.jamal.tools.Params.holder;

/**
 * Inner classes implement macros that ease the handling of document references to files and directories.
 */
public class FilesMacro {

    /**
     * Check that the directory exists, and it is a directory.
     */
    public static class Directory implements Macro, InnerScopeDependent {
        private static class Trie {
            static final IndexedPlaceHolders formatter = IndexedPlaceHolders.with(
                    // snippet dirMacroFormatPlaceholders
                    "$name", // gives the name of the directory as was specified on the macro
                    "$absolutePath",  // gives the name of the directory as was specified on the macro
                    "$parent", // the parent directory
                    "$canonicalPath" // the canonical path
                    // end snippet
            );
        }

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var format = holder("directoryFormat", "format").orElse("$name").asString();
            final var root = holder("root").orElse("").as(String.class, FileTools::trailDirectory);
            Scan.using(processor).from(this).between("()").keys(format, root).parse(in);
            final var name = in.toString().trim();
            final var dirName = Paths.get(FileTools.absolute(in.getReference(), root.get() + name)).normalize().toString();
            final var dir = new File(dirName.length() > 0 ? dirName : ".");
            if (!dir.exists()) {
                throw new BadSyntaxAt("The directory '" + dirName + "' does not exist.", in.getPosition());
            }
            if (!dir.isDirectory()) {
                throw new BadSyntaxAt("The directory '" + dirName + "' exists but it is not a directory.", in.getPosition());
            }

            try {
                return Trie.formatter.format(format.get(), value(name), value(dir.getAbsolutePath()), value(dir.getParent()), value(dir::getCanonicalPath));
            } catch (Exception e) {
                // cannot really happen
                throw new BadSyntaxAt("Directory name '" + dirName
                        + "' cannot be formatted using the given format '"
                        + format.get() + "'", in.getPosition(), e);
            }
        }

        @Override
        public String getId() {
            return "directory";
        }
    }

    /**
     * Check that the file exists, and it is a file.
     */
    public static class FileMacro implements Macro, InnerScopeDependent {
        private static class Trie {
            static final IndexedPlaceHolders formatter = IndexedPlaceHolders.with(
                    // snippet fileMacroFormatPlaceholders
                    "$name", // gives the name of the file as was specified on the macro
                    "$absolutePath", // the absolute path to the file
                    "$parent", // the parent directory where the file is
                    "$simpleName",  // the name of the file without the path
                    "$canonicalPath" // the canonical path
                    // end snippet
            );
        }

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var format = holder("fileFormat", "format").orElse("$name").asString();
            final var root = holder("root").orElse("").as(String.class, FileTools::trailDirectory);
            Scan.using(processor).from(this).between("()").keys(format, root).parse(in);
            final var name = in.toString().trim();
            final var fileName = FileTools.absolute(in.getReference(), root.get() + name);
            final var file = new File(fileName);
            if (!file.exists()) {
                throw new BadSyntaxAt("The file '" + file.getAbsolutePath() + "' does not exist.", in.getPosition());
            }
            if (!file.isFile()) {
                throw new BadSyntaxAt("The file '" + file.getAbsolutePath() + "' exists but it is not a plain file.", in.getPosition());
            }

            try {
                return Trie.formatter.format(format.get(), value(name), value(file.getAbsolutePath()), value(file.getParent()), value(file::getName), value(file::getCanonicalPath));
            } catch (Exception e) {
                // cannot really happen
                throw new BadSyntaxAt("Directory name '" + fileName
                        + "'cannot be formatted using the given format '"
                        + format + "'", in.getPosition(), e);
            }
        }

        @Override
        public String getId() {
            return "file";
        }
    }


}
