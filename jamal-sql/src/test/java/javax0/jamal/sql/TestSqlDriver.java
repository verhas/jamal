package javax0.jamal.sql;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestSqlDriver {

    @Test
    @DisplayName("Test that the macro can load the driven specified by the name of the class")
    void testSqlDriverLoad() throws Exception {
        TestThat.theInput("{@sql:driver org.h2.Driver}").results("");
    }
}
