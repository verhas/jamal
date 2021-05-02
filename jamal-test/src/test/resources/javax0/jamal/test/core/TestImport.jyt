package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestImport {

    @Test
    void testImport() throws Exception {
        TestThat.theInput(
            "{@import res:import/included_as_resource.jim}\n" +
                "{hello World}{hella me}"
        ).results(

            "\nHello, World!Hella, me!"

        );
    }
}
