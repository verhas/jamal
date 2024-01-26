package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TestPlural {

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
    void testCalLingPluralizer(String word, String plural) throws Exception {
        TestThat.theInput("{@plural " + word + "}").results(plural);
    }

    @Test
    void testDictionary() throws Exception {
        TestThat.theInput("{@plural child=children}{@plural child}").results("children");
    }
    @Test
    void testDictionaryRetrieved() throws Exception {
        TestThat.theInput("{@plural mikka} {@plural makka}").results("mikkas makkas");
    }
}
