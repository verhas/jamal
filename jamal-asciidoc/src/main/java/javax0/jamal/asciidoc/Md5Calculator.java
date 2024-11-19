package javax0.jamal.asciidoc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for calculating MD5 hashes of text and binary content.
 * <p>
 * This class provides methods to compute an MD5 hash for a given {@code String} or byte array
 * and returns the result as a Base64 encoded string.
 * </p>
 */
public class Md5Calculator {

    /**
     * Computes the MD5 hash of the given text content.
     *
     * @param content the text content to hash, encoded as UTF-8
     * @return the Base64 encoded MD5 hash of the content, or {@code null} if the MD5 algorithm is unavailable
     */
    static String md5(final String content) {
        return md5(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Computes the MD5 hash of the given binary content.
     *
     * @param content the byte array to hash
     * @return the Base64 encoded MD5 hash of the content, or {@code null} if the MD5 algorithm is unavailable
     */
    static String md5(final byte[] content) {
        try {
            final var digester = MessageDigest.getInstance("MD5");
            digester.update(content);
            return Base64.getEncoder().encodeToString(digester.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
