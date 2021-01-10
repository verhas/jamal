package javax0.jamal.builtinstest;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class TestIncludeDepth {

    @Test
    void testIncludeDepthFailsBeforeStackOverflow() {
        Assertions.assertThrows(BadSyntax.class, () -> TestThat.theInput("{@include res:recinclude.jim}").results(""));
    }

    @Test
    void testIncludeDepthDoesNotFailsForMultipleIncludesOnSameLevel() throws Exception {
        TestThat.theInput("{@include res:null.jim}".repeat(200)).results("");
    }
}
