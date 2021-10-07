package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.Params;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// snippet SnipCheck
public class SnipCheck implements Macro {

    private static final String ALGORITHM = "SHA-256";
    private static final int MIN_LENGTH = 6;

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var hashString = Params.<String>holder("hash", "hashCode").orElse("");
        final var id = Params.<String>holder("id");
        final var fileName = Params.<String>holder("file");
        Params.using(processor).from(this).keys(hashString, id, fileName).parse(in);
        if (id.isPresent() && fileName.isPresent()) {
            throw new BadSyntax("You cannot specify 'id' and 'file' the same time for snip:check");
        }
        final String snippet;
        if (id.isPresent()) {
            snippet = SnippetStore.getInstance(processor).snippet(id.get());
        } else if (fileName.isPresent()) {
            var reference = in.getReference();
            var absoluteFileName = FileTools.absolute(reference, fileName.get());
            snippet = FileTools.getInput(absoluteFileName).toString();
        } else {
            throw new BadSyntax("You have to specify either 'id' or 'fileName' for snip:check");
        }

        final MessageDigest md;
        try {
            md = MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException nsae) {
            // must not happen, because we use SHA-256 and the Java standard requres that
            //
            //Every implementation of the Java platform is required to support the following standard MessageDigest algorithms:
            //
            //MD5
            //SHA-1
            //SHA-256
            throw new BadSyntax("There is no algorithm '" + ALGORITHM + "' to create the digest for snippet consistency check.", nsae);
        }
        final var bytes = md.digest(snippet.getBytes(StandardCharsets.UTF_8));
        final var hashStringCalculated = HexDumper.encode(bytes);
        if (hashString.get().length() < MIN_LENGTH) {
            if (id.isPresent()) {
                throw new BadSyntax("The snippet '" + id.get() + "' hash is '" + hashStringCalculated + "'.");
            } else {
                throw new BadSyntax("The file '" + fileName.get() + "' hash is '" + hashStringCalculated + "'.");
            }
        }
        if (hashStringCalculated.endsWith(hashString.get())) {
            return "";
        }
        if (id.isPresent()) {
            throw new BadSyntax("The snippet '" + id.get() + "' hash is '" + hashStringCalculated + "' does not end with '" + hashString.get() + "'.");
        } else {
            throw new BadSyntax("The file '" + fileName.get() + "' hash is '" + hashStringCalculated + "' does not end with '" + hashString.get() + "'.");
        }
    }

    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Override
    public String getId() {
        return "snip:check";
    }
}
//end snippet
