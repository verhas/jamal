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
import java.util.Arrays;
import java.util.stream.Collectors;

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
            BadSyntaxAt.when(!dir.exists(), () -> "The directory '" + dirName + "' does not exist.",in.getPosition());
            BadSyntaxAt.when(!dir.isDirectory(), () -> "The directory '" + dirName + "' exists but it is not a directory.",in.getPosition());

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
                    "$simpleName", // the name of the file without the path
                    "$canonicalPath", // the canonical path
                    "$bareNaked", // the file name without the extensions
                    "$naked1", // the file name without the last extension
                    "$naked2", // the file name without the last 2 extensions
                    "$naked3", // the file name without the last 3 extensions
                    "$naked4", // the file name without the last 4 extensions
                    "$naked5", // the file name without the last 5 extensions
                    "$extensions", // the file name extensions
                    "$extension1", // the file name last extension
                    "$extension2", // the file name last 2 extensions
                    "$extension3", // the file name last 3 extensions
                    "$extension4", // the file name last 4 extensions
                    "$extension5"  // the file name last 5 extensions
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
            BadSyntaxAt.when(!file.exists(), () -> "The file '" + file.getAbsolutePath() + "' does not exist.",in.getPosition());
            BadSyntaxAt.when(!file.isFile(), () -> "The file '" + file.getAbsolutePath() + "' exists but it is not a plain file.",in.getPosition());

            try {
                return Trie.formatter.format(format.get(),
                        value(name),
                        value(file.getAbsolutePath()),
                        value(file.getParent()),
                        value(file::getName),
                        value(file::getCanonicalPath),
                        value(new FileClosure(file, 0)::nakedFileName),
                        value(new FileClosure(file, 1)::nakedFileNameN),
                        value(new FileClosure(file, 2)::nakedFileNameN),
                        value(new FileClosure(file, 3)::nakedFileNameN),
                        value(new FileClosure(file, 4)::nakedFileNameN),
                        value(new FileClosure(file, 5)::nakedFileNameN),
                        value(new FileClosure(file, 0)::extensions),
                        value(new FileClosure(file, 1)::extensionsN),
                        value(new FileClosure(file, 2)::extensionsN),
                        value(new FileClosure(file, 3)::extensionsN),
                        value(new FileClosure(file, 4)::extensionsN),
                        value(new FileClosure(file, 5)::extensionsN)
                );
            } catch (Exception e) {
                // cannot really happen
                throw new BadSyntaxAt("Directory name '" + fileName
                        + "'cannot be formatted using the given format '"
                        + format + "'", in.getPosition(), e);
            }
        }

        private static class FileClosure {
            final File f;
            final int i;

            private FileClosure(final File f, final int i) {
                this.f = f;
                this.i = i;
            }

            /**
             * Return the file name omitting the last {@code i} extensions.
             * If there are less than {@code i} extensions then return the file name without extension.
             *
             * @return the file name without the extensions
             */
            private String nakedFileNameN() {
                final var nameParts = f.getName().split("\\.");
                final var needed = nameParts.length - i;
                if (needed < 1) {
                    return nameParts[0];
                } else {
                    return Arrays.stream(nameParts).limit(needed).collect(Collectors.joining("."));
                }
            }

            /**
             * Calculate the file name without any extensions.
             *
             * @return the file name without the extensions.
             */
            private String nakedFileName() {
                return f.getName().split("\\.")[0];
            }

            /**
             * Return the file name omitting the last {@code i} extensions.
             * If there are less than {@code i} extensions then return the file name without extension.
             *
             * @return the file name without the extensions
             */
            private String extensionsN() {
                final var nameParts = f.getName().split("\\.");
                final var skip = i >= nameParts.length ? 1 : nameParts.length - i;
                return Arrays.stream(nameParts).skip(skip).collect(Collectors.joining("."));
            }

            /**
             * get the extensions of the file
             *
             * @return all the extensions of the file name
             */
            private String extensions() {
                return Arrays.stream(f.getName().split("\\.")).skip(1).collect(Collectors.joining("."));
            }
        }

        @Override
        public String getId() {
            return "file";
        }
    }


}
