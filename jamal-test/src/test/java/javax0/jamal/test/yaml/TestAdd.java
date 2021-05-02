package javax0.jamal.test.yaml;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestAdd {

    @Test
    void testAddingToAMapWithKeyToRoot() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "}" +
            "{@yaml:add to=a key=c\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "}" +
            "{a}"
        ).results(
            "a: this is a\n" +
                "b: this is b\n" +
                "c: {a: this is a, b: this is b}\n"
        );
    }

    @Test
    void testAddingUnresolves() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "}" +
            "{@yaml:isResolved a}\n" +
            "{@yaml:resolve a}" +
            "{@yaml:isResolved a}\n" +
            "{@yaml:add to=a key=c\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "}" +
            "{@yaml:isResolved a}\n"
        ).results("" +
            "false\n" +
            "true\n" +
            "false\n"
        );
    }

    @Test
    void testAddingToListToRoot() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "- this is a\n" +
            "- this is b\n" +
            "}" +
            "{@yaml:add to=a\n" +
            "this is c\n" +
            "}" +
            "{@yaml:add to=a\n" +
            "this is d\n" +
            "}" +
            "{a}"
        ).results(
            "[this is a, this is b, this is c, this is d]\n"
        );
    }

    @Test
    void testAddingToAMapWithKey() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: {}\n" +
            "}" +
            "{@yaml:add to=a.c key=huppa\n" +
            "\"abraka dabra\"\n" +
            "}" +
            "{a}"
        ).results(
            "a: this is a\n" +
                "b: this is b\n" +
                "c: {huppa: abraka dabra}\n"
        );
    }

    @Test
    void testAddingToAMapWithKeyFlattened() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: {}\n" +
            "}" +
            "{@yaml:add to=a.c key=huppa flatten\n" +
            "\"abraka dabra\"\n" +
            "}" +
            "{a}"
        ).throwsBadSyntax("You cannot 'yaml:add' with a 'key' parameter when flattening for 'a.c'");
    }

    @Test
    void testAddingToAMapFattened() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: {}\n" +
            "}" +
            "{@yaml:add to=a.c flat\n" +
            "huppa: \"abraka dabra\"\n" +
            "muppa: \"abraka kadarka vinoe\"" +
            "}" +
            "{a}"
        ).results(
            "a: this is a\n" +
                "b: this is b\n" +
                "c: {huppa: abraka dabra, muppa: abraka kadarka vinoe}\n"
        );
    }

    @Test
    void testAddingListToAMapFattened() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: {}\n" +
            "}" +
            "{@yaml:add to=a.c flat\n" +
            "- huppa: \"abraka dabra\"\n" +
            "- muppa: \"abraka kadarka vinoe\"" +
            "}" +
            "{a}"
        ).throwsBadSyntax("You can add only a Map to a Map when flat\\(ten\\) for 'a.c'");
    }

    @Test
    void testAddingToList() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: []\n" +
            "}" +
            "{@yaml:add to=a.c\n" +
            "this is c first\n" +
            "}" +
            "{@yaml:add to=a.c\n" +
            "this is c second\n" +
            "}" +
            "{a}"
        ).results(
            "a: this is a\n" +
                "b: this is b\n" +
                "c: [this is c first, this is c second]\n"
        );
    }

    @Test
    void testAddingToListFlattened() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: []\n" +
            "}" +
            "{@yaml:add to=a.c flat\n" +
            "- this is c first\n" +
            "- this is c second\n" +
            "}" +
            "{a}"
        ).results(
            "a: this is a\n" +
                "b: this is b\n" +
                "c: [this is c first, this is c second]\n"
        );
    }

    @Test
    void testAddingMapToListFlattened() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: []\n" +
            "}" +
            "{@yaml:add to=a.c flat\n" +
            "karoly: this is c first\n" +
            "sebestyen: this is c second\n" +
            "}" +
            "{a}"
        ).throwsBadSyntax("You can add only a List to a List when flat\\(tten\\) for 'a.c'");
    }

    @Test
    void testAddToMapWithoutKey() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: []\n" +
            "}" +
            "{@yaml:add to=a\n" +
            "this is c first\n" +
            "}"
        ).throwsBadSyntax("You cannot 'yaml:add' without a 'key' parameter to a Map for.*");
    }

    @Test
    void testAddToListWithKey() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "- a: this is a\n" +
            "- b: this is b\n" +
            "- c: []\n" +
            "}" +
            "{@yaml:add to=a key=birca\n" +
            "this is c first\n" +
            "}"
        ).throwsBadSyntax("You cannot 'yaml:add' with a 'key' parameter to a List for .*");
    }

    @Test
    void testAddToNonListNonMap() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "}" +
            "{@yaml:add to=a.a key=birca\n" +
            "this is c first\n" +
            "}"
        ).throwsBadSyntax("You can 'yaml:add' only to a List or Map for 'a.a'\n" +
            "The actual class is class java.lang.String");
    }

    @Test
    void testAddWithOgnlToNull() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a\n" +
            "}" +
            "{@yaml:add to=a.zuppa key=birca\n" +
            "this is c first\n" +
            "}"
        ).throwsBadSyntax("Cannot 'yaml:add' into the OGN expression 'a.zuppa'");
    }

    @Test
    void testAddWithOgnlPointsToBadStructure() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "- this is a\n" +
            "}" +
            "{@yaml:add to=a.zuppa key=birca\n" +
            "this is c first\n" +
            "}"
        ).throwsBadSyntax("Cannot 'yaml:add' into the OGN expression 'a.zuppa'");
    }

    @Test
    void testAddWithWrongOgnl() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "- this is a\n" +
            "}" +
            "{@yaml:add to=a.zuppa!hh key=birca\n" +
            "this is c first\n" +
            "}"
        ).throwsBadSyntax("Syntax error in the OGNL expression 'a.zuppa!hh'");
    }

    @Test
    void testAddWithMalformedYaml() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "- this is a\n" +
            "}" +
            "{@yaml:add to=a\n" +
            "- khmm\n" +
            "z: 2" +
            "this is c first\n" +
            "}"
        ).throwsBadSyntax("Cannot load YAML data.");
    }
}
