package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class TestCast {

    @Test
    void testNullAndEmptyString() throws BadSyntax {
        assertEquals("", Cast.cast(null));
        assertEquals("", Cast.cast(""));
    }

    @Test
    void testIntegerCasting() throws BadSyntax {
        assertEquals(42, Cast.cast("(int)42"));
    }

    @Test
    void testLongCasting() throws BadSyntax {
        assertEquals(1234567890123L, Cast.cast("(long)1234567890123"));
    }

    @Test
    void testShortCasting() throws BadSyntax {
        assertEquals((short) 32000, Cast.cast("(short)32000"));
    }

    @Test
    void testByteCasting() throws BadSyntax {
        assertEquals((byte) 127, Cast.cast("(byte)127"));
    }

    @Test
    void testDoubleCasting() throws BadSyntax {
        assertEquals(3.14, Cast.cast("(double)3.14"));
    }

    @Test
    void testFloatCasting() throws BadSyntax {
        assertEquals(2.71f, Cast.cast("(float)2.71"));
    }

    @Test
    void testBooleanCasting() throws BadSyntax {
        assertTrue((Boolean) Cast.cast("(boolean)true"));
        assertFalse((Boolean) Cast.cast("(boolean)false"));
    }

    @Test
    void testBigDecimalCasting() throws BadSyntax {
        assertEquals(new BigDecimal("12345.6789"), Cast.cast("(BigDecimal)12345.6789"));
    }

    @Test
    void testCharCasting() throws BadSyntax {
        assertEquals('A', Cast.cast("(char)A"));
    }

    @Test
    void testStringCasting() throws BadSyntax {
        assertEquals("hello", Cast.cast("(string)hello"));
    }

    @Test
    void testUnwrappedString() throws BadSyntax {
        assertEquals("noCast", Cast.cast("noCast"));
    }

    @Test
    void testUnsupportedType() {
        assertThrows(BadSyntax.class, () -> Cast.cast("(unsupported)123"));
    }
}
