package javax0.jamal.snippet.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static javax0.jamal.snippet.tools.Pluralizer.pluralize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestPluralizer {

    @ParameterizedTest
    @CsvSource({
            "bus, buses",
            "church, churches",
            "fox, foxes",
            "buzz, buzzes",
            "dish, dishes",
            "cat, cats",
            "dog, dogs",
            "baby, babies",
            "lady, ladies",
            "day, days",
            "key, keys",
            "nastaliqiy, nastaliqiys",
            "boy, boys",
            "guy, guys",
            "Bus, Buses",
            "Church, Churches",
            "Fox, Foxes",
            "Buzz, Buzzes",
            "Dish, Dishes",
            "Cat, Cats",
            "Dog, Dogs",
            "Baby, Babies",
            "Lady, Ladies",
            "Day, Days",
            "Key, Keys",
            "Nastaliqiy, Nastaliqiys",
            "Boy, Boys",
            "Guy, Guys",
            "buS, buSES",
            "churCH, churCHES",
            "churCh, churChEs",
            "churcH, churcHeS",
            "foX, foXES",
            "buzZ, buzZES",
            "diSH, diSHES",
            "diSh, diShEs",
            "disH, disHeS",
            "caT, caTS",
            "doG, doGS",
            "babY, babIES",
            "ladY, ladIES",
            "daY, daYS",
            "keY, keYS",
            "nastaliqiY, nastaliqiYS",
            "nastaliqIy, nastaliqIys",
            "boY, boYS",
            "guY, guYS"
    })
    void test(String word, String plural) {
        assertEquals(plural, pluralize(word));
    }

    @Test
    void testEmptyString() {
        assertEquals("", pluralize(""));
    }

    @Test
    public void testNullInput() {
        assertNull(pluralize(null));
    }
}
