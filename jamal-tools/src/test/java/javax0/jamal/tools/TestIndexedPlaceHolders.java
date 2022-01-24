package javax0.jamal.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static javax0.jamal.tools.IndexedPlaceHolders.value;

public class TestIndexedPlaceHolders {

    private static class Format {
        IndexedPlaceHolders iph;
        String[] values;

        String format(String string) throws Exception {
            return iph.format(string, values);
        }
    }

    private Format with(String... strings) {
        final var f = new Format();
        final var keys = new String[strings.length / 2];
        f.values = new String[strings.length / 2];
        for (int i = 0; i < strings.length; i += 2) {
            keys[i / 2] = strings[i];
            f.values[i / 2] = strings[i + 1];
        }
        f.iph = IndexedPlaceHolders.with(keys);
        return f;
    }

    @Test
    @DisplayName("Works in the simple case")
    void testPlaceHolderReplace() throws Exception {
        Assertions.assertEquals("a , b , b , b , $c",
                with("$a", "a", "$b", "b").format("$a , $b , $b , $b , $c"));
    }

    @Test
    @DisplayName("Does not replace replaced code documentation")
    void testPlaceHolderReplaceComplex0() throws Exception {
        Assertions.assertEquals("$b $a",
                with("$a", "$b", "$b", "$a").format("$a $b"));
    }

    @Test
    @DisplayName("Does not replace replaced code")
    void testPlaceHolderReplaceComplex() throws Exception {
        Assertions.assertEquals("a , b , $b",
                with("$a", "a", "$b", "b").format("$a , $b , $$b"));
    }

    @Test
    @DisplayName("Works on empty input string")
    void testEmpty() throws Exception {
        Assertions.assertEquals("",
                with("$a", "a", "$b", "b").format(""));
    }

    @Test
    @DisplayName("Works with empty map")
    void testEmptyMap() throws Exception {
        Assertions.assertEquals("abraka dabra",
                with().format("abraka dabra"));
    }

    @Test
    @DisplayName("Works on string with empty map")
    void testEmptyAll() throws Exception {
        Assertions.assertEquals("",
                with().format(""));
    }

    @Test
    @DisplayName("Does not skip placeholder after long placeholder")
    void testWorksWithLongerValue() throws Exception {
        Assertions.assertEquals("$b $a",
                with("$thisIsAReallyLongPlaceHolder", "$b", "$b", "$a").format("$thisIsAReallyLongPlaceHolder $b"));
        Assertions.assertEquals("this is a long value that at the end contains a placeholder like string $b $a",
                with("$a", "this is a long value that at the end contains a placeholder like string $b", "$b", "$a").format("$a $b"));
    }

    @Test
    @DisplayName("Does not replace back and forth")
    void testVeryComplex() throws Exception {
        Assertions.assertEquals("$b $a",
                with("$a", "$b", "$b", "$a").format("$a $b"));
    }

    @Test
    @DisplayName("Does not throw supplier exception when the placeholder is not used")
    void testNotThrowing() throws Exception {
        final var formatter = IndexedPlaceHolders.with("$a", "$b");
        Assertions.assertEquals("$b $b",
                formatter.format("$a $a", value("$b"), value(() -> {
                    throw new Exception();
                })));
    }

    @Test
    @DisplayName("Throws exception when the placeholder is used")
    void testThrowing() {
        final var formatter = IndexedPlaceHolders.with("$a", "$b");
        Assertions.assertThrows(Exception.class,() ->
                formatter.format("$a $b", value("$b"), value(() -> {
                    throw new Exception();
                })));
    }

    @Test
    @DisplayName("Supplier is evaluated only once")
    void testEvalOnce() throws Exception {
        final var i = new AtomicInteger(1);
        final var formatter = IndexedPlaceHolders.with("$a");
        Assertions.assertEquals("1 1",
                formatter.format("$a $a", value( () -> "" + i.getAndAdd(1))));
    }

}
