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
        final var hashFile = scanner.str(null, "hashFile").optional();
        final var hashCode = scanner.str(null, "hashCode").optional();
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

This macro allows for the evaluation of input just once and subsequent reevaluation only if there has been a change.
It proves beneficial in executing macros that produce external resources based on the document's text, preventing unnecessary repetition of resource generation if the text remains unchanged.
It is particularly useful, for instance, in memoizing PlantUML or other image generation processes.
By utilizing this macro, images are generated solely when the text that impacts the generation process is modified.

The macro has three parameters:

* The `file` parameter represents the name of the file that is generated.
This parameter can be specified multiple times, allowing for multiple files.
The macro neither reads nor writes this file.
Instead, it checks for the file's existence.
If the file does not exist, the macro will then evaluate its input, which is expected to generate the file in some manner.
However, the actual generation of the file is beyond the scope of this macro.

+
When no `file` is specified, the macro assumes that the result already exists.
This is applicable in scenarios where certain calculations do not result in file generation and the outcome is stored elsewhere.
Under such circumstances, checking for a file is deemed unnecessary.

* The hashCode parameter allows for the specification of the hash value or the hash code of the text that influences the generation.
If not explicitly defined, this value is automatically calculated based on the macro's input.
The hashCode macro can be utilized within the document for this calculation.
Generally, manually inserting a hash value into the document is unnecessary.
Instead, it's more typical to calculate it based on text that differs from the macro's direct input.

+
For instance, consider a scenario where you're generating a PlantUML diagram that also incorporates additional files during its creation process.
The text from these included files won't be a part of the macro's direct input.
Nevertheless, you'd want the diagram generation to occur even if only the included text files undergo modifications.
In such situations, you can utilize the `hashCode` macro to compute the hash code of the verbatim text from the included files.
Subsequently, this computed hash code can be employed as the `hashCode` parameter in the `memoize` macro, ensuring the diagram is regenerated when the included files change, even if the main input to the macro remains the same.

* The hashFile parameter denotes the name of the file that stores the hash value.
Before the macro processes its input, it compares the current hash value with the one stored in the hashFile.
If they match, indicating no changes, the macro does not reevaluate the input.
Conversely, if the hash values differ, or in situations where the hashFile does not exist, the macro proceeds to create the file and reevaluates the input.

The macro's return value is the result of the evaluated input when reevaluation occurs, and it returns an empty string if no evaluation is performed.
This feature is particularly beneficial during interactive editing, as it provides a clear indication of whether the macro was executed.
If you don't require this functionality, you can opt to use macros that don't produce any output.

end snippet*/
