package javax0.jamal.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestGenericTrie {


    @Test
    @DisplayName("Finds a single element put in as string")
    void testGet() {
        final var sut = new GenericTrie<Integer>();
        sut.put("a", 13);
        Assertions.assertEquals((Integer) 13, sut.get("a"));
    }

    @Test
    @DisplayName("Null values can also be inserted")
    void testNull() {
        final var sut = new GenericTrie<Integer>();
        sut.put("a", null);
        Assertions.assertNull(sut.get("a"));
    }

    @Test
    @DisplayName("get() return null for non-existent key")
    void testNotFind() {
        final var sut = new GenericTrie<Integer>();
        sut.put("a", 13);
        Assertions.assertNull(sut.get("b"));
    }

    @Test
    @DisplayName("Throws for illegal character")
    void testIllegalCharacter1() {
        final var sut = new GenericTrie<Integer>();
        sut.put("a", 1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("\n", 1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("a\n", 1));
        sut.put("ba", 1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("bc\n", 1));

    }

    @Test
    @DisplayName("The map stores and finds example keys")
    void testMultipleExamples() {
        final var sut = new GenericTrie<Integer>();
        sut.put("a", 1);
        sut.put("ba", 1);
        sut.put("bb", 1);
        sut.put("bca", 1);
        sut.put("bda", 1);
        sut.put("bdc", 1);

        Assertions.assertEquals((Integer)1, sut.get("a"));
        Assertions.assertEquals((Integer)1, sut.get("ba"));
        Assertions.assertEquals((Integer)1, sut.get("bb"));
        Assertions.assertEquals((Integer)1, sut.get("bca"));
        Assertions.assertEquals((Integer)1, sut.get("bda"));
        Assertions.assertEquals((Integer)1, sut.get("bdc"));
    }

    @Test
    @DisplayName("Throws exception when key prefix each other")
    void testThrowsForPrefix() {
        final var sut = new GenericTrie<String>();
        sut.put("bca", "bca");
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("bcad", "bcad"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("bc", "bc"));
    }

    @Test
    @DisplayName("Throws exception when key prefix each other even when the values are null")
    void testThrowsForPrefixForNullValues() {
        final var sut = new GenericTrie<String>();
        sut.put("bca", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("bcad", null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("bc", null));
    }

    @Test
    @DisplayName("The map stores and finds example keys")
    void testFind() {
        final var sut = new GenericTrie<String>();
        sut.put("a", "a");
        sut.put("ba", "ba");
        sut.put("bb", "bb");
        sut.put("bca", "bca");
        sut.put("bda", "bda");
        sut.put("bdc", "bdc");

        final var result1 = sut.find("abba");

        //noinspection OptionalGetWithoutIsPresent
        Assertions.assertEquals(0, result1.get().start);
        Assertions.assertEquals(1, result1.get().end);
        final var s = "\nkirbgobdchuppa";
        final var result2 = sut.find(s);
        //noinspection OptionalGetWithoutIsPresent
        Assertions.assertEquals("bdc", s.substring(result2.get().start, result2.get().end));

        final var result3 = sut.find("sssspppbc");
        Assertions.assertFalse(result3.isPresent());
    }

}
