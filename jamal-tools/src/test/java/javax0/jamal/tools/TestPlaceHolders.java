package javax0.jamal.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TestPlaceHolders {

    @Test
    @DisplayName("Works in the simple case")
    void testPlaceHolderReplace() throws Exception {
        Assertions.assertEquals("a , b , b , b , $c",
            PlaceHolders.of("$a", "a", "$b", "b").format("$a , $b , $b , $b , $c"));
    }

    @Test
    @DisplayName("Does not replace replaced code documentation")
    void testPlaceHolderReplaceComplex0() throws Exception {
        Assertions.assertEquals("$b $a",
            PlaceHolders.of("$a", "$b", "$b", "$a").format("$a $b"));
    }

    @Test
    @DisplayName("Does not replace replaced code")
    void testPlaceHolderReplaceComplex() throws Exception {
        Assertions.assertEquals("a , b , $b",
            PlaceHolders.of("$a", "a", "$b", "b").format("$a , $b , $$b"));
    }

    @Test
    @DisplayName("Works on empty input string")
    void testEmpty() throws Exception {
        Assertions.assertEquals("",
            PlaceHolders.of("$a", "a", "$b", "b").format(""));
    }

    @Test
    @DisplayName("Works with empty map")
    void testEmptyMap() throws Exception {
        Assertions.assertEquals("abraka dabra",
            PlaceHolders.of().format("abraka dabra"));
    }

    @Test
    @DisplayName("Works on string with empty map")
    void testEmptyAll() throws Exception {
        Assertions.assertEquals("",
            PlaceHolders.of().format(""));
    }

    @Test
    @DisplayName("Does not replace back and forth")
    void testVeryComplex() throws Exception {
        Assertions.assertEquals("$b $a",
            PlaceHolders.of("$a", "$b", "$b", "$a").format("$a $b"));
    }

    @Test
    @DisplayName("Does not throw supplier exception when the placeholder is not used")
    void testNotThrowing() throws Exception {
        Assertions.assertEquals("$b $b",
            PlaceHolders.of("$a", "$b").and("$b", () -> {
                throw new Exception();
            }).format("$a $a"));
    }

    @Test
    @DisplayName("Throws exception when the placeholder is used")
    void testThrowing() throws Exception {
        Assertions.assertThrows(Exception.class, () ->
            PlaceHolders.of("$a", "$b").and("$b", () -> {
                throw new Exception();
            }).format("$a $b"));
    }

    @Test
    @DisplayName("Supplier is evaluated only once")
    void testEvalOnce() throws Exception {
        final var i = new AtomicInteger(1);
        Assertions.assertEquals("1 1",
            PlaceHolders.of().and("$a", () -> "" + i.getAndAdd(1)).format("$a $a"));
    }

}
