package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class TestMemoize {

    /**
     * The memoize macro called two times. It is evaluated the first time because the test deletes any existing {@code
     * hash.code} file. The second time it is not evaluated because the {@code hash.code} file exists and the hash code
     * is the same.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Test that memoize remembers not to run the second time")
    void testMemoizedRunning() throws Exception {
        final var file = new File("hash.code");
        if (file.exists()) {
            Assumptions.assumeTrue(file.delete());
        }
        TestThat.theInput("" +
                "{@memoize (hashFile=hash.code)this is the returned value}" +
                "{@memoize (hashFile=hash.code)this is the returned value}" +
                ""
        ).results("this is the returned value");
    }

    /**
     * This test executes the memoize macro twice. The first version provides the hashCode. It is the hash code of the
     * second content. It does not matter that the hash code of the first content is not this value. It is provided,
     * it is used. The second time it does not evaluate, because it is memoized.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Test that memoize remembers not to run the second time for given hash code")
    void testMemoizedRunningGivenHashCode() throws Exception {
        final var file = new File("hash.code");
        if (file.exists()) {
            Assumptions.assumeTrue(file.delete());
        }
        TestThat.theInput("" +
                "{@memoize (hashCode=55a3ea8154bf3e9001e91fe0ecb086277fadb6c57ec90318626fa180088a9600 hashFile=hash.code)whatever}" +
                "{@memoize (hashFile=hash.code)this is the returned value}" +
                ""
        ).results("whatever");
    }

    /**
     * If there are no files and no hash code, then the memoize sees no reason to run the content.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Test what happens when no file or hash code is provided")
    void testMemoizedRunningWithoutParameters() throws Exception {
        final var file = new File("hash.code");
        if (file.exists()) {
            Assumptions.assumeTrue(file.delete());
        }
        TestThat.theInput("" +
                "{@memoize A}{@memoize A}" +
                ""
        ).results("");
    }

    /**
     * This test executes the memoize macro twice. The first version provides the hashCode. It is the hash code of the
     * second content. It does not matter that the hash code of the first content is not this value. It is provided,
     * it is used. The second time it does not evaluate, because it is memoized.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Test that memoize remembers not to run the second time for given hash code")
    void testMemoizedRunningGivengFiles() throws Exception {
        final var FILES = List.of("hash1.code", "hash2.code", "hash3.code");
        for (final String s : FILES) {
            final var file = new File(s);
            Files.write(file.toPath(), "000".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        final var file = new File("hash.code");
        if (file.exists()) {
            Assumptions.assumeTrue(file.delete());
        }
        TestThat.theInput("" +
                "{@memoize (file=hash1.code)NO PRINT}" +
                "{@memoize (file=hash0.code)there is no hash0.code first,}" +
                "{@memoize (file=hash0.code file=hash1.code) second,}" +
                "{@memoize (file=hash0.code file=hash1.code file=hash2.code) and third time}" +
                ""
        ).results("there is no hash0.code first, second, and third time");

        // tear down
        for (final String s : FILES) {
            final var file1 = new File(s);
            if (file1.exists()) {
                Assumptions.assumeTrue(file1.delete());
            }
        }
    }


}
