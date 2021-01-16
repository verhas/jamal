package javax0.jamal.engine.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class TestSeparatorCalculator {

    @Test
    void testWithAFewShortStrings() {
        final var sut = new SeparatorCalculator("abc");
        Assertions.assertEquals("b", sut.calculate("a"));
        Assertions.assertEquals("c", sut.calculate("ab"));
        Assertions.assertEquals("aa", sut.calculate("abc"));
        Assertions.assertEquals("ac", sut.calculate("abcaa"));
        Assertions.assertEquals("ba", sut.calculate("abcaac"));
        Assertions.assertEquals("bb", sut.calculate("abcaacba"));
        Assertions.assertEquals("cc", sut.calculate("abcaacbabb"));
        Assertions.assertEquals("aaa", sut.calculate("abcaacbabbcc"));
    }

    @Test
    void testOneCharacterAlphabet() {
        final var sut = new SeparatorCalculator("a");
        final var sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            final var sep = sut.calculate(sb.toString());
            sb.append('a');
            Assertions.assertEquals(sb.toString(), sep);
        }
    }

    @Test
    void testInterruptedString(){
        final var sut = new SeparatorCalculator("abc");
        Assertions.assertEquals("c", sut.calculate("a b"));
        Assertions.assertEquals("aa", sut.calculate("a b c"));
        Assertions.assertEquals("ba", sut.calculate("a b c aab ac "));

    }

    final static private int SIZE = 200;

    @Test
    void testRandomly() {
        final var chars = "abcd ";
        final var rnd = new Random();
        for (int j = 0; j < 10; j++) {
            final var sb = new StringBuilder();
            for (int i = 0; i < SIZE; i++) {
                sb.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            final var sut = new SeparatorCalculator(chars.substring(0,chars.length()-1));
            final var sep = sut.calculate(sb.toString());
            Assertions.assertFalse(sb.toString().contains(sep));
        }
    }

}
