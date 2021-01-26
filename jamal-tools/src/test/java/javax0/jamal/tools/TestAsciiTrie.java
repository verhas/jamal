package javax0.jamal.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;

public class TestAsciiTrie {

    @Test
    @DisplayName("The size is zero after creation")
    void testSizeZero() {
        final var sut = new AsciiTrie<String>();
        Assertions.assertEquals(0, sut.size());
    }

    @Test
    @DisplayName("The map is empty  after creation")
    void testEmpty() {
        final var sut = new AsciiTrie<String>();
        Assertions.assertTrue(sut.isEmpty());
    }

    @Test
    @DisplayName("The size is 1 after adding one element")
    void testSizeOne() {
        final var sut = new AsciiTrie<String>();
        sut.put("a", "b");
        Assertions.assertEquals(1, sut.size());
    }

    @Test
    @DisplayName("The map is not empty when the size is not zero")
    void testNotEmpty() {
        final var sut = new AsciiTrie<String>();
        sut.put("a", "b");
        Assertions.assertFalse(sut.isEmpty());
    }

    @Test
    @DisplayName("The size is 0 after removing the last element")
    void testSizeZeroAgain() {
        final var sut = new AsciiTrie<String>();
        sut.put("a", "b");
        Assertions.assertEquals("b", sut.remove("a"));
        Assertions.assertEquals(0, sut.size());
    }

    @Test
    @DisplayName("The size does not change removing non-existent element")
    void testSizeOneAgain() {
        final var sut = new AsciiTrie<String>();
        sut.put("a", "b");
        Assertions.assertNull(sut.remove("c"));
        Assertions.assertEquals(1, sut.size());
    }


    private String random(int length) {
        int leftLimit = '!';
        int rightLimit = '~';
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int)
                (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    @Test
    @DisplayName("The map stores and finds keys")
    void testManyKeys() {
        final var sut = new AsciiTrie<String>();
        final var ref = new HashSet<String>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                final var s = random(i);
                ref.add(s);
                sut.put(s, s);
            }
        }
        for( final var s : ref ){
            Assertions.assertEquals(s,sut.get(s));
        }
    }

}
