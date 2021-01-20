package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestUpdate {
    @Test
    void testUpdate() throws Exception {
        TestThat.theInput("{@include src/test/resources/javax0/jamal/snippet/document_update_test.jam}").results("");
    }
}
