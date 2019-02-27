package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

class TestFor {

    @Test
    @DisplayName("The for loop goes through the elements in the list.")
    void testForMacro() throws
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            BadSyntax,
            IllegalAccessException {
        TestThat.forMacro(For.class).fromInput(" x in (a,b,c,d)= x is either a, b, c or d\n").results(
                " a is either a, b, c or d\n" +
                        " b is either a, b, c or d\n" +
                        " c is either a, b, c or d\n" +
                        " d is either a, b, c or d\n");
    }

    @Test
    @DisplayName("When the ( and ) are missing around the list it is bad syntax")
    void testForBadSyntax() throws
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            BadSyntax,
            IllegalAccessException {
        TestThat.forMacro(For.class).fromInput(" x in a,b,c,d= x is either a, b, c or d\n").throwsBadSyntax();
    }

    @Test
    @DisplayName("List separator can be defined in the user defined macro $forsep")
    void testForDefinedSeparator() throws
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            BadSyntax,
            IllegalAccessException {
        TestThat.forMacro(For.class)
                .define("$forsep", ";")
                .fromInput(" x in (a;b;c;d)= x is either a, b, c or d\n").results(
                " a is either a, b, c or d\n" +
                        " b is either a, b, c or d\n" +
                        " c is either a, b, c or d\n" +
                        " d is either a, b, c or d\n");
    }
}
