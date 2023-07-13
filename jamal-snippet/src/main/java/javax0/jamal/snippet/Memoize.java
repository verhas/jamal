package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.SHA256;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.ListParameter;
import javax0.jamal.tools.param.StringParameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.stream.Stream;

public class Memoize implements Macro, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var files = scanner.list(null, "file");
        final var hashFile = scanner.str(null, "hashFile").defaultValue(null);
        final var hashCode = scanner.str(null, "hashCode").defaultValue(null);
        scanner.done();

        final String hash = getHashValue(in, hashFile, hashCode);
        final String fn = hashFile.isPresent() ? FileTools.absolute(in.getReference(), hashFile.get()) : null;
        if (fileMissing(in.getReference(), files) || hashCodeDoesNotMatch(fn, hash)) {
            writeHashFileNewValue(fn, hash);
            return processor.process(in);
        } else {
            return "";
        }
    }

    /**
     * Write the hash value to the file.
     *
     * @param file the hash file where to write the new hash value
     * @param hash the new hash value
     * @throws BadSyntax when there is some issue writing the file
     */
    private static void writeHashFileNewValue(String file, String hash) throws BadSyntax {
        if (file != null) {
            try {
                Files.writeString(new File(file).toPath(), hash, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new BadSyntax("Cannot write the hash file '" + file + "'", e);
            }
        }
    }

    /**
     * Check that the hash code matches the one reference the file.
     *
     * @param file the name of the hash file where the hash is stored as text. It has to be the absolute path.
     * @param hash the hash value to compare the file content to
     * @return {@code true} if the hash code does not match the file content. {@code false} otherwise.
     */
    private static boolean hashCodeDoesNotMatch(String file, String hash) {
        if (file == null) {
            return false;
        }
        final var f = Optional.of(file)
                .map(File::new)
                .filter(File::exists);
        if (f.isEmpty()) { // when there is no file to read, it means the hash code does not match
            return true;
        }
        final byte[] bytes;
        try {
            bytes = Files.readAllBytes(f.get().toPath());
        } catch (IOException e) {
            // if you cannot read the file, it does not match
            return true;
        }
        return Stream.of(bytes)
                .map(b -> new String(b, StandardCharsets.UTF_8))
                .noneMatch(s -> s.equals(hash));
    }

    /**
     * Check if any of the files is missing.
     *
     * @param reference the reference file, to which the files can be relative to. It is the file where the input is from.
     * @param files     the list of the files that have to exist
     * @return {@code true} if any of the files does not exist. {@code false} otherwise.
     * @throws BadSyntax if there is some problem calculating the file name
     */
    private static boolean fileMissing(String reference, ListParameter files) throws BadSyntax {
        return files.stream()
                .map(f -> FileTools.absolute(reference, f))
                .map(File::new)
                .anyMatch(f -> !f.exists());
    }

    /**
     * Get the hash value from the input, the given parameters or calculate it.
     *
     * @param in       the input used to calculate the hash value if not provided.
     * @param hashFile the hash file parameter. It is only used to decide if there is a need for a hash value at all.
     *                 The mthod only checks that it is specified, but does nothing with the file name of the file.
     *                 If no hash file is defined then the method returns {@code null}, no hash check will be necessary.
     * @param hashCode the hash code defined. If this is not present, then the hash value will be calculated. If it is
     *                 present, then this is the hash value.
     * @return the hash value. If it is provided in hashCode, then that. If not, then it is calculated from the input unless
     * the hashFile is not present. In that case the method returns {@code null}.
     * @throws BadSyntax if there is some problem with the parameters handling.
     */
    private static String getHashValue(Input in, StringParameter hashFile, StringParameter hashCode) throws BadSyntax {
        final String hash;
        if (hashFile.isPresent()) {
            if (!hashCode.isPresent()) {
                hash = HexDumper.encode(SHA256.digest(in.toString()));
            } else {
                hash = hashCode.get();
            }
        } else {
            hash = null;
        }
        return hash;
    }

}
/* snippet Memoize

This macro can be used to evaluate some input only one and reevaluate only when it has been changed.
This has been useful to execute macros that generate some external resources from some text in the document and do not want to repeat the generation if the text has not changed.
You may want to memoize, for example, PlantUML or other picture generation.
Using this macro, you can generate the picture only when the text influencing the generation has changed.

The macro has three parameters:

* `file` is the name of the file that is generated.
You can have multiple files specified, this parameter can be repeated.
The macro does not read this file or write this file.
It simply checks that the file exists or not.
If the file does not exist, then the macro will evaluate its input.
The input evaluation is supposed to generate the file some way.
The file generation is out of the scope of this macro.

+
When there is no `file` specified, then the macro will assume that the result is there.
Some calculations may not generate file and the result is somewhere else.
In that case, there is no point to check a file.

* `hashCode` is the hash value the hash code of the text influencing the generation.
If it is not defined, the value is calculated automatically from the input of the macro.
You can use the macro `hashCode` in the document to make the calculation.
Usually there is no point manually inserting a hash value into the document.
You want to calculate it from some text other than the pure input of the macro.

+
For example, you generate a PlantUML picture, but it also includes some other files during the PlantUML generation.
In this case, the text included will not be part of the input of the macro.
You still want to execute the picture generation even when only the included text file is changed.
In that case, you can use the `hashCode` macro on the verbatim included files and use the result as the `hashCode` parameter of the `memoize` macro.

* `hashFile` is the name of the file that contains the hash value.
Before evaluating the input, the macro checks that the hash value is the same as the one in the file.
The macro does not evaluate the input if they are the same.
If they differ, including the special case, when the hash file does not exist, then the macro generates the file and evaluates the input.

The return value of the macro is the input evaluated when it is evaluated and an empty string when it is not evaluated.
This functionality helps to see evidence during interactive editing then the macro was evaluated.
You can just use macros that generate no output if you do not need this feature.

end snippet*/
