package javax0.jamal.scriptbasic;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestMacros {
    @DisplayName("Test all files that have an '.expected' pair")
    @Test
    void testExpectedFiles() throws IOException, BadSyntax {
        TestAll.testExpected(this, Assertions::assertEquals);
    }

}
