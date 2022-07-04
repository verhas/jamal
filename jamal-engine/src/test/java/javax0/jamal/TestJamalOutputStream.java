package javax0.jamal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestJamalOutputStream {

    @Test
    @DisplayName("JamalOutputStream writes the input to the output after being processed by Jamal")
    void test() throws Exception {
        final var baos = new ByteArrayOutputStream();
        try (final var sut = new JamalOutputStream(baos)) {
            sut.write(("" +
                    "{@define name=Petike}Hello, {name}!"
            ).getBytes(StandardCharsets.UTF_8));
        }
        baos.close();
        baos.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals("Hello, Petike!", baos.toString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("JamalOutputStream throws IO exception if there is an error")
    void testException() throws Exception {
        final var baos = new ByteArrayOutputStream();
        final var sut = new JamalOutputStream(baos);
        sut.write(("" +
                "Hello, {name}!"
        ).getBytes(StandardCharsets.UTF_8));
        Assertions.assertThrows(IOException.class, sut::close);
    }
}
