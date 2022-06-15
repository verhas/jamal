package javax0.jamal.test.core;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class TestNullMacro {

    @Test
    @DisplayName("Macro opening string follwoed by the macro closing string will result the macro opening string")
    void testNullMacro() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        TestThat.theInput("{}").results("{");
    }
}
