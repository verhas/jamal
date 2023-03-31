package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestReferences {

    private static void cleanRefFile() throws Exception {
        final var path = Paths.get("ref.jrf");
        if( Files.exists(path) ) {
            Files.delete(path);
        }
    }

    @DisplayName("Test argument less macro references")
    @Test
    void testReferences() throws Exception {
        cleanRefFile();
        TestThat.theInput("{@references}{@ref a}{a}{@define a=1}").matches("WARNING The reference file .*[/\\\\]jamal-snippet[/\\\\]ref.jrf was not found.UNDEFINED");
        TestThat.theInput("{@references}{@ref a}{a}").results("1");
        cleanRefFile();
    }

    @DisplayName("Test macro references with arguments")
    @Test
    void testReferencesWithArgs() throws Exception {
        cleanRefFile();
        TestThat.theInput("{@references}{@ref a}{a/1}{@define a($x)=$x}").matches("WARNING The reference file .*[/\\\\]jamal-snippet[/\\\\]ref.jrf was not found.UNDEFINED");
        TestThat.theInput("{@references}{@ref a}{a/1}").results("1");
        cleanRefFile();
    }

}
