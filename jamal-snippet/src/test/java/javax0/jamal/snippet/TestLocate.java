package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static javax0.jamal.DocumentConverter.getRoot;

public class TestLocate {

    @Test
    @DisplayName("locale using find() name only")
    void testLocateFindFile() throws Exception {
        final var root = getRoot();
        TestThat.theInput("{@file:locate format=$simpleName isFile find=\"uleName.ext\"}")
                .atPosition(root + "/jamal-snippet/src/test/resources/reldirt1/z.txt", 1, 1)
                .results("fuleName.ext");
    }

    @Test
    @DisplayName("locale using find() name only does not find because it is not directory")
    void testLocateFindDirectory() throws Exception {
        final var root = getRoot();
        TestThat.theInput("{@file:locate format=$simpleName isDirectory find=\"uleName.ext\"}")
                .atPosition(root + "/jamal-snippet/src/test/resources/reldirt1/z.txt", 1, 1)
                .throwsBadSyntax("No file matching 'uleName.ext' found in.*");
    }

    @Test
    @DisplayName("locale directory using find() name only does not find because it is not directory")
    void testLocateFindDirectorySucc() throws Exception {
        final var root = getRoot();
        TestThat.theInput("{@file:locate format=$simpleName isDirectory find=\"c\"}")
                .atPosition(root + "/jamal-snippet/src/test/resources/reldirt1/z.txt", 1, 1)
                .results("c");
    }

    @Test
    @DisplayName("locale using match() name only")
    void testLocateMatchFile() throws Exception {
        final var root = getRoot();
        TestThat.theInput("{@file:locate format=$simpleName match=\".uleName\\\\..{3}\"}")
                .atPosition(root + "/jamal-snippet/src/test/resources/reldirt1/z.txt", 1, 1)
                .results("fuleName.ext");
    }

    @Test
    @DisplayName("locale using match() fullPath")
    void testLocateMatchFilePath() throws Exception {
        final var root = getRoot();
        TestThat.theInput("{@file:locate format=$simpleName fullPath match=\".*jamal.*uleName\\\\..{3}\"}")
                .atPosition(root + "/jamal-snippet/src/test/resources/reldirt1/z.txt", 1, 1)
                .results("fuleName.ext");
    }

    @Test
    @DisplayName("locale using find() fullPath")
    void testLocateFindFilePath() throws Exception {
        final var root = getRoot();
        TestThat.theInput("{@file:locate format=$simpleName fullPath find=\"uleName\\\\..{3}\"}")
                .atPosition(root + "/jamal-snippet/src/test/resources/reldirt1/z.txt", 1, 1)
                .results("fuleName.ext");
    }
}
