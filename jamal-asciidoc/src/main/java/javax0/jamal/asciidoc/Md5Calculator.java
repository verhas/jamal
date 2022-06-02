package javax0.jamal.asciidoc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Md5Calculator {
    static String md5(final String content) {
        try {
            final var digester = MessageDigest.getInstance("MD5");
            digester.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digester.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
