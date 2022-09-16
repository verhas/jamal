package javax0.jamal.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestConnectionStringParser {

    @Test
    void testHttpConnectionString() {
        final var sut = new ConnectionStringParser("http:param1:param2:param3?option1=1&&option2");
        Assertions.assertEquals("http", sut.getProtocol());
        Assertions.assertEquals(3, sut.getParameters().length);
        Assertions.assertEquals("param1", sut.getParameters()[0]);
        Assertions.assertEquals("param2", sut.getParameters()[1]);
        Assertions.assertEquals("param3", sut.getParameters()[2]);
        Assertions.assertEquals("1", sut.getOption("option1").get());
        Assertions.assertEquals("", sut.getOption("option2").get());
    }

    @Test
    void testNoParams() {
        final var sut = new ConnectionStringParser("protocol");
        Assertions.assertEquals("protocol", sut.getProtocol());
        Assertions.assertEquals(0, sut.getParameters().length);
    }

    @Test
    void testEmptyString() {
        final var sut = new ConnectionStringParser("");
        Assertions.assertEquals("", sut.getProtocol());
        Assertions.assertEquals(0, sut.getParameters().length);
    }
}
