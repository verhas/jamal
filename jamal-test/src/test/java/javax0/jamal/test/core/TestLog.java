package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLog {

    @Test
    void testLogging() throws Exception {
        final var log = new StringBuilder();
        final var sut = TestThat.theInput("{@log [level=DEBUG]debug message}");
        final var processor = sut.getProcessor();
        processor.setLogger((level, pos, message, params) -> log.append(level).append(" ").append(pos.file).append(" ").append(String.format(message, (Object[]) params)));
        sut.results("");
        Assertions.assertEquals("DEBUG null debug message", log.toString());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
