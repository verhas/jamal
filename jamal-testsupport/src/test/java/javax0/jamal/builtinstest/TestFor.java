package javax0.jamal.builtinstest;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.builtins.For;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
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
        TestThat.theMacro(For.class).fromTheInput(" x in (a,b,c,d)= x is either a, b, c or d\n").results(
            " a is either a, b, c or d\n" +
                " b is either a, b, c or d\n" +
                " c is either a, b, c or d\n" +
                " d is either a, b, c or d\n");
    }

    @Test
    @DisplayName("The for loop goes through the elements in the list using special loop var.")
    void testForMacroArbitraryArgument() throws
        InvocationTargetException,
        NoSuchMethodException,
        InstantiationException,
        BadSyntax,
        IllegalAccessException {
        TestThat.theMacro(For.class).fromTheInput(" (x/=(!=+//) in (a,b,c,d)= x/=(!=+// is either a, b, c or d\n").results(
            " a is either a, b, c or d\n" +
                " b is either a, b, c or d\n" +
                " c is either a, b, c or d\n" +
                " d is either a, b, c or d\n");
    }

    @Test
    @DisplayName("The for loop with multiple variables.")
    void testForMultiFor() throws
        InvocationTargetException,
        NoSuchMethodException,
        InstantiationException,
        BadSyntax,
        IllegalAccessException {
        TestThat.theMacro(For.class).fromTheInput(" (X1,X2,X3) in (a|h|k,b|w|x,c|0|2,d|4|bruhaha)= X1 X2 X3\n").results(
            " a h k\n" +
                " b w x\n" +
                " c 0 2\n" +
                " d 4 bruhaha\n");
    }

    @Test
    @DisplayName("The for loop with multiple variables and empty values.")
    void testForMultiForEmptyTag() throws
        InvocationTargetException,
        NoSuchMethodException,
        InstantiationException,
        BadSyntax,
        IllegalAccessException {
        TestThat.theMacro(For.class).fromTheInput(" (X1,X2,X3) in (a||k,b|w|,c|0|2,|4|bruhaha)= X1 X2 X3\n").results(
            " a  k\n" +
                " b w \n" +
                " c 0 2\n" +
                "  4 bruhaha\n");
    }

    @Test
    @DisplayName("The macro for is inner scope dependent, when used with # the $forsep and $forsubsep can be defined inside")
    void testForInnerScopeDependence() throws InvocationTargetException, NoSuchMethodException, InstantiationException, BadSyntax, IllegalAccessException {
        TestThat.theInput("{#for $a in (a:b:c)={@define $forsep=:}a is $a\n}{?$forsep}").results("a is a\n" +
            "a is b\n" +
            "a is c\n");
    }

    @Test
    @DisplayName("Test that the for loop throws exception when not in lenient mode and work lenient in case it is lenient")
    void testForLeniency() throws InvocationTargetException, NoSuchMethodException, InstantiationException, BadSyntax, IllegalAccessException {
        TestThat.theInput("{@try!{#for ($a,$b) in (a|b,b|,c)=$a}}\n------\n{@options lenient}{#for ($a,$b) in (a|b,b|x|y|h,c)=$a$b\n}")
            .results("number of the values does not match the number of the parameters\n" +
                "$a,$b\n" +
                "c\n" +
                "------\n" +
                "ab\n" +
                "bx\n" +
                "c\n");
    }

    @Test
    @DisplayName("The for loop multiple variables some containing the other")
    void testForParametersContainingOtherThrows() {
        Assertions.assertThrows(BadSyntaxAt.class, () ->
            TestThat.theMacro(For.class).fromTheInput(" (X,X2,X3) in (a|h|k,b|w|x,c|0|2,d|4|!)= X X2 X3\n").results(""));
    }

    @Test
    @DisplayName("When the ( and ) are missing around the list it is bad syntax")
    void testForBadSyntax() throws
        InvocationTargetException,
        NoSuchMethodException,
        InstantiationException,
        IllegalAccessException {
        TestThat.theMacro(For.class).fromTheInput(" x in a,b,c,d= x is either a, b, c or d\n").throwsBadSyntax();
    }

    @Test
    @DisplayName("List separator can be defined in the user defined macro $forsep")
    void testForDefinedSeparator() throws
        InvocationTargetException,
        NoSuchMethodException,
        InstantiationException,
        BadSyntax,
        IllegalAccessException {
        TestThat.theMacro(For.class)
            .define("$forsep", ";")
            .fromTheInput(" x in (a;b;c;d)= x is either a, b, c or d\n").results(
            " a is either a, b, c or d\n" +
                " b is either a, b, c or d\n" +
                " c is either a, b, c or d\n" +
                " d is either a, b, c or d\n");
    }
}
