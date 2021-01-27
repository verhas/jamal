package javax0.jamal.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestTrie {

    @Test
    @DisplayName("Finds a single element put in as string")
    void testGet() throws Exception {
        final var sut = new Trie();
        sut.put("a", "b");
        Assertions.assertEquals("b", sut.get("a"));
    }

    @Test
    @DisplayName("Null values can also be inserted")
    void testNull() throws Exception {
        final var sut = new Trie();
        sut.put("a", (String)null);
        Assertions.assertNull(sut.get("a"));
    }
    @Test
    @DisplayName("Finds a single element put in as string supplier")
    void testGetFromSUpplier() throws Exception {
        final var sut = new Trie();
        sut.put("a", () -> "b");
        Assertions.assertEquals("b", sut.get("a"));
    }

    @Test
    @DisplayName("get() return null for non-existent key")
    void testNotFind() throws Exception {
        final var sut = new Trie();
        sut.put("a", "b");
        Assertions.assertNull(sut.get("b"));
    }

    @Test
    @DisplayName("Throws for illegal character")
    void testIllegalCharacter1() {
        final var sut = new Trie();
        sut.put("a", "a");
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("\n", "a"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("a\n", "a"));
        sut.put("ba", "ba");
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("bc\n", "b"));

    }

    @Test
    @DisplayName("The map stores and finds example keys")
    void testMultipleExamples() throws Exception {
        final var sut = new Trie();
        sut.put("a", "a");
        sut.put("ba", "ba");
        sut.put("bb", "bb");
        sut.put("bca", "bca");
        sut.put("bda", "bda");
        sut.put("bdc", "bdc");

        Assertions.assertEquals("a", sut.get("a"));
        Assertions.assertEquals("ba", sut.get("ba"));
        Assertions.assertEquals("bb", sut.get("bb"));
        Assertions.assertEquals("bca", sut.get("bca"));
        Assertions.assertEquals("bda", sut.get("bda"));
        Assertions.assertEquals("bdc", sut.get("bdc"));
    }

    @Test
    @DisplayName("Throws exception when key prefix each other")
    void testThrowsForPrefix() {
        final var sut = new Trie();
        sut.put("bca", "bca");
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("bcad", "bcad"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("bc", "bc"));
    }

    @Test
    @DisplayName("Throws exception when key prefix each other even when the values are null")
    void testThrowsForPrefixForNullValues() {
        final var sut = new Trie();
        sut.put("bca", (String)null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("bcad", (String)null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sut.put("bc", (String)null));
    }

    @Test
    @DisplayName("The map stores and finds example keys")
    void testFind() throws Exception {
        final var sut = new Trie();
        sut.put("a", "a");
        sut.put("ba", "ba");
        sut.put("bb", "bb");
        sut.put("bca", "bca");
        sut.put("bda", "bda");
        sut.put("bdc", "bdc");

        final var result1 = sut.find("abba");

        Assertions.assertEquals(0, result1.get().start);
        Assertions.assertEquals(1, result1.get().end);
        final var s = "\nkirbgobdchuppa";
        final var result2 = sut.find(s);
        Assertions.assertEquals("bdc", s.substring(result2.get().start, result2.get().end));

        final var result3 = sut.find("sssspppbc");
        Assertions.assertFalse(result3.isPresent());
    }


}
