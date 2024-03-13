package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestReferences {

    private static void cleanRefFile() throws Exception {
        final var path = Paths.get("ref.jrf");
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @DisplayName("Test argument less macro references")
    @Test
    void testReferences() throws Exception {
        cleanRefFile();
        TestThat.theInput("{@references}{@ref a}{a}{@define a=1}").throwsBadSyntax("The reference file .*[/\\\\]jamal-snippet[/\\\\]ref.jrf was not found.");
        TestThat.theInput("{@references}{@ref a}{a}").results("1");
        cleanRefFile();
    }

    @DisplayName("Test idempotency error: macro is not referenced / deleted")
    @Test
    void testReferencesNonIdempotent1() throws Exception {
        cleanRefFile();
        TestThat.theInput("{@references}{@ref a}{@ref b}{a}{@define a=1}").throwsBadSyntax("The reference file .*[/\\\\]jamal-snippet[/\\\\]ref.jrf was not found.");
        TestThat.theInput("{@references}{@ref a}{a}").throwsBadSyntax("The following references are not idempotent: macro 'b' is deleted.*");
        cleanRefFile();
    }

    @DisplayName("Test idempotency error: macro is new")
    @Test
    void testReferencesNonIdempotent2() throws Exception {
        cleanRefFile();
        TestThat.theInput("{@references}{@ref a}{a}{@define a=1}").throwsBadSyntax("The reference file .*[/\\\\]jamal-snippet[/\\\\]ref.jrf was not found.");
        TestThat.theInput("{@references}{@ref a}{@define c=3}{@ref c}{a}").throwsBadSyntax("The following references are not idempotent: macro 'c' is new.*");
        cleanRefFile();
    }

    @DisplayName("Test idempotency error: macro has changed")
    @Test
    void testReferencesNonIdempotent3() throws Exception {
        cleanRefFile();
        TestThat.theInput("{@references}{@ref a}{a}{@define a=1}").throwsBadSyntax("The reference file .*[/\\\\]jamal-snippet[/\\\\]ref.jrf was not found.");
        TestThat.theInput("{@references}{@ref a}{@define a=3}{a}").throwsBadSyntax("The following references are not idempotent: macro 'a' has changed.*");
        cleanRefFile();
    }


    @DisplayName("Test macro references with arguments")
    @Test
    void testReferencesWithArgs() throws Exception {
        cleanRefFile();
        TestThat.theInput("{@references}{@ref a}{a/1}{@define a($x)=$x}").throwsBadSyntax("The reference file .*[/\\\\]jamal-snippet[/\\\\]ref.jrf was not found.");
        TestThat.theInput("{@references}{@ref a}{a/1}").results("1");
        cleanRefFile();
    }

    @Test@DisplayName("Test refrence closing gracefully when error occurs")
    void testReferencesInError() throws Exception {
        cleanRefFile();
        // the first time it reports that there is no reference file, this is more an assumption, the preparation of the real test when there is a faulty reference file
        TestThat.theInput("{@references}{@ref a}{a}{@define a=1{@define a=2}").throwsBadSyntax("The reference file .* was not found\\.");
        // check that the real processing error is part of the exception message, not only the faulty reference file
        TestThat.theInput("{@references}{@define z={@ref a}{a}{@define a=1}{@define a=2}").throwsBadSyntax("Macro was not terminated in the file.*");
        cleanRefFile();
    }

}
