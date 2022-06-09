package javax0.jamal.mock;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class TestLogging {

    @Test
    @DisplayName("Mock logs a warning when repeat is zero")
    void testLogging() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        //                                            0000000000111111111122222222223
        //                                            0123456789012345678901234567890
        final var test = TestThat.theInput("{@mock (repeat=0 id=wakka)wakka}");
        test.results("");
        final var logs = test.getLogs();
        Assertions.assertEquals(1,logs.size());
        Assertions.assertEquals("[WARNING] Repeat is zero. at null/1:27",logs.get(0));
    }
}
