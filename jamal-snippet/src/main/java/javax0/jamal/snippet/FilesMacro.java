package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.IndexedPlaceHolders;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.StringParameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax0.jamal.tools.IndexedPlaceHolders.value;

/**
 * Inner classes implement macros that ease the handling of document references to files and directories.
 */
public class FilesMacro {
    private static class Trie {
        static final IndexedPlaceHolders formatter = IndexedPlaceHolders.with(
                // snippet fileMacroFormatPlaceholders
                "$name",    // gives the name of the file as was specified on the macro
                "$absolutePath",  // the absolute path to the file
                "$relativePath",  // the relative path to the file (absolute as for now)
                "$parent",        // the parent directory where the file is
                "$simpleName",    // the name of the file without the path
                "$canonicalPath", // the canonical path
                "$bareNaked",     // the file name without the extensions
                "$naked1",        // the file name without the last extension
                "$naked2",        // the file name without the last 2 extensions
                "$naked3",        // the file name without the last 3 extensions
                "$naked4",        // the file name without the last 4 extensions
                "$naked5",        // the file name without the last 5 extensions
                "$extensions",    // the file name extensions
                "$extension1",    // the file name last extension
                "$extension2",    // the file name last 2 extensions
                "$extension3",    // the file name last 3 extensions
                "$extension4",    // the file name last 4 extensions
                "$extension5",    // the file name last 5 extensions
                "$time",          // the time of the last modification
                "$ctime",         // the creation time
                "$atime"         // the last access time
                // end snippet
        );
    }

    private static String getInputFileLocation(Input in) {
        if (in != null && in.getPosition() != null && in.getPosition().top() != null && in.getPosition().top().file != null) {
            return in.getPosition().top().file;
        }
        return new File(".").getAbsolutePath();
    }

    /**
     * Check that the directory exists, and it is a directory.
     */
    public static class Directory implements Macro, InnerScopeDependent, Scanner {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var scanner = newScanner(in, processor);
            final var format = scanner.str("directoryFormat", "format").defaultValue("$name");
            final var root = scanner.str("root").defaultValue("").getParam().as(String.class, FileTools::trailDirectory);
            final var dateFormat = scanner.str("dateFormat").defaultValue("yyyy-MM-dd HH:mm:ss");
            final var relativeTo = scanner.str("relativeTo").defaultValue(getInputFileLocation(in));
            scanner.done();

            final var name = in.toString().trim();
            final var dirName = Paths.get(FileTools.absolute(in.getReference(), root.get() + name)).normalize().toString();
            final File dir;
            try {
                dir = new File(dirName.isEmpty() ? "." : dirName).getCanonicalFile();
            } catch (IOException e) {
                throw new BadSyntax("Error reading the file '" + dirName + "'", e);
            }
            BadSyntaxAt.when(!dir.exists(), "The directory '" + dirName + "' does not exist.", in.getPosition());
            BadSyntaxAt.when(!dir.isDirectory(), "The directory '" + dirName + "' exists but it is not a directory.", in.getPosition());

            try {
                return formatString(format, name, dir, dateFormat.get(), relativeTo.get());
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
    public static class FileMacro implements Macro, InnerScopeDependent, Scanner {


        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var scanner = newScanner(in, processor);
            final var format = scanner.str("fileFormat", "format").defaultValue("$name");
            final var root = scanner.str("root").defaultValue("").getParam().as(String.class, FileTools::trailDirectory);
            final var dateFormat = scanner.str("dateFormat").defaultValue("yyyy-MM-dd HH:mm:ss");
            final var relativeTo = scanner.str("relativeTo").defaultValue(getInputFileLocation(in));
            scanner.done();
            final var name = in.toString().trim();
            final var fileName = FileTools.absolute(in.getReference(), root.get() + name);
            final File file;
            try {
                file = new File(fileName).getCanonicalFile();
            } catch (IOException e) {
                throw new BadSyntax("Error reading the file '" + fileName + "'", e);
            }
            BadSyntaxAt.when(!file.exists(), "The file '" + file.getAbsolutePath() + "' does not exist.", in.getPosition());
            BadSyntaxAt.when(!file.isFile(), "The file '" + file.getAbsolutePath() + "' exists but it is not a plain file.", in.getPosition());

            try {
                return formatString(format, name, file, dateFormat.get(), relativeTo.get());
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

    private static String formatString(final StringParameter format,
                                       final String name,
                                       final File dirOrFile,
                                       final String dateFormat,
                                       final String relativeTo
    ) throws Exception {
        final var df = new SimpleDateFormat(dateFormat);
        return Trie.formatter.format(format.get(),
                value(name),
                value(dirOrFile.getAbsolutePath()),
                value(FileTools.getRelativePath( new File(relativeTo),dirOrFile)),
                value(dirOrFile.getParent()),
                value(dirOrFile::getName),
                value(dirOrFile::getCanonicalPath),
                value(new FileClosure(dirOrFile, 0)::nakedFileName),
                value(new FileClosure(dirOrFile, 1)::nakedFileNameN),
                value(new FileClosure(dirOrFile, 2)::nakedFileNameN),
                value(new FileClosure(dirOrFile, 3)::nakedFileNameN),
                value(new FileClosure(dirOrFile, 4)::nakedFileNameN),
                value(new FileClosure(dirOrFile, 5)::nakedFileNameN),
                value(new FileClosure(dirOrFile, 0)::extensions),
                value(new FileClosure(dirOrFile, 1)::extensionsN),
                value(new FileClosure(dirOrFile, 2)::extensionsN),
                value(new FileClosure(dirOrFile, 3)::extensionsN),
                value(new FileClosure(dirOrFile, 4)::extensionsN),
                value(new FileClosure(dirOrFile, 5)::extensionsN),
                value(new FileClosure(dirOrFile, df)::mtime),
                value(new FileClosure(dirOrFile, df)::ctime),
                value(new FileClosure(dirOrFile, df)::atime)
        );
    }

    private static class FileClosure {
        final File f;
        final int i;

        final SimpleDateFormat df;

        private FileClosure(final File f, final int i) {
            this.f = f;
            this.i = i;
            this.df = null;
        }

        private FileClosure(final File f, final SimpleDateFormat df) {
            this.f = f;
            this.i = 0;
            this.df = df;
        }

        private String mtime() throws BadSyntax {
            return time(BasicFileAttributes::lastModifiedTime);
        }

        private String atime() throws BadSyntax {
            return time(BasicFileAttributes::lastAccessTime);
        }

        private String ctime() throws BadSyntax {
            return time(BasicFileAttributes::creationTime);
        }

        private String time(Function<BasicFileAttributes, FileTime> timeGetter) throws BadSyntax {
            return df.format(new Date(timeGetter.apply(getAttrs(f)).toMillis()));
        }

        private static BasicFileAttributes getAttrs(final File f) throws BadSyntax {
            try {
                return Files.readAttributes(Paths.get(f.getAbsolutePath()), BasicFileAttributes.class);
            } catch (IOException e) {
                throw new BadSyntax("Cannot read creation time", e);
            }
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
}
