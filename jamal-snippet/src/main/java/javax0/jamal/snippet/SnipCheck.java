package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

// snippet SnipCheck
public class SnipCheck implements Macro {

    private static final String ALGORITHM = "SHA-256";
    private static final int MIN_LENGTH = 6;

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var hashString = Params.<String>holder("hash", "hashCode").orElse("");
        Params.using(processor).from(this).between("()").keys(hashString).parse(in);
        skipWhiteSpaces(in);
        final var id = fetchId(in);
        final var snippet = SnippetStore.getInstance(processor).snippet(id);

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
        final var hashStringCalculated = byteArrayToHex(bytes);
        if (hashString.get().length() < MIN_LENGTH) {
            throw new BadSyntax("The snippet '" + id + "' hash is '" + hashStringCalculated + "'.");
        }
        if (hashStringCalculated.endsWith(hashString.get())) {
            return "";
        }
        throw new BadSyntax("The snippet '" + id + "' hash is '" + hashStringCalculated + "' does not end with '" + hashString.get() + "'.");
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
