package javax0.jamal.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestPlaceHolder {

    @Test
    @DisplayName("Works in the simple case")
    void testPlaceHolderReplace() {
        Assertions.assertEquals("a , b , b , b , $c",
            PlaceHolder.replace("$a , $b , $b , $b , $c", Map.of("$a", "a", "$b", "b")));
    }

    @Test
    @DisplayName("Does not replace replaced code")
    void testPlaceHolderReplaceComplex() {
        Assertions.assertEquals("a , b , $b",
            PlaceHolder.replace("$a , $b , $$b", Map.of("$a", "a", "$b", "b")));
    }

    @Test
    @DisplayName("Works on empty input string")
    void testEmpty() {
        Assertions.assertEquals("",
            PlaceHolder.replace("", Map.of("$a", "a", "$b", "b")));
    }

    @Test
    @DisplayName("Works with empty map")
    void testEmptyMap() {
        Assertions.assertEquals("abraka dabra",
            PlaceHolder.replace("abraka dabra", Map.of()));
    }

    @Test
    @DisplayName("Works on string with empty map")
    void testEmptyAll() {
        Assertions.assertEquals("",
            PlaceHolder.replace("", Map.of()));
    }

    @Test
    @DisplayName("Does not replace back and forth")
    void testVeryComplex() {
        Assertions.assertEquals("$b $a",
            PlaceHolder.replace("$a $b", Map.of("$a", "$b", "$b", "$a")));
    }

}
