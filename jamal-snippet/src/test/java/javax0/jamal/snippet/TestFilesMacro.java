package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFilesMacro {

    /**
     * Find the project root directory based on the fact that this is the only directory that has a {@code ROOT.dir}
     * file. Start with the current working directory and works up maximum 10 directories.
     *
     * @return the canonical path of the project root directory
     * @throws IOException if the directory structure cannot be read
     */
    static String getDirectory() throws IOException {
        var rootDir = new StringBuilder("ROOT.dir");
        var counter = new AtomicInteger(10);
        File rootDirFile;
        while (true) {
            rootDirFile = new File(rootDir.toString());
            if (rootDirFile.exists()) {
                break;
            }
            rootDir.insert(0, "../");
            Assertions.assertTrue(counter.decrementAndGet() > 0,
                "Cannot find the ROOT.dir file.");
        }
        final var parent = rootDirFile.getParentFile();
        return parent == null ? "." : parent.getCanonicalPath();
    }

    @Test
    @DisplayName("Directory is found and formatted")
    void testDirectory() throws Exception {
        final var root = getDirectory() + "/jamal-snippet/src/main/java/javax0/jamal/snippet/";
        TestThat.theInput("{@define directoryFormat=$canonicalPath}{#replace {@options regex} |{@directory ./}|^.*?main|main}")
            .atPosition(root,0,0)
            .results("main/java/javax0/jamal/snippet");
        TestThat.theInput("{@define directoryFormat=$canonicalPath}{#replace {@options regex} |{@directory ..}|^.*?main|main}")
            .atPosition(root,0,0)
            .results("main/java/javax0/jamal");
        TestThat.theInput("{@define root=../../../}{@directory javax0/jamal}")
            .atPosition(root,0,0)
            .results("javax0/jamal");
    }

    @Test
    @DisplayName("Directory is found and formatted using defined root")
    void testDirectoryWithRoot() throws Exception {
        TestThat.theInput("{@define root=../}{@directory jamal-snippet/src}").results("jamal-snippet/src");
    }

    @Test
    @DisplayName("Directory macro throws exception if directory does not exist")
    void testDirectoryThrowsNonExistent() throws Exception {
        TestThat.theInput("{@directory abraka/dabra/cicadas/bite/my/donkey}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Directory macro throws exception if file exists but not directory")
    void testDirectoryThrowsOnFile() throws Exception {
        TestThat.theInput("{@directory ./README.adoc}").throwsBadSyntax();
    }

    @Test
    @DisplayName("File is found and formatted")
    void testFile() throws Exception {
        TestThat.theInput("{@define fileFormat=$canonicalPath}{#replace {@options regex} |{@file ./README.adoc}|^.*?jamal-|jamal-}").results("jamal-snippet/README.adoc");
        TestThat.theInput("{@define root=../}{@define fileFormat=`$name`}{@file README.adoc}").results("`README.adoc`");
    }

    @Test
    @DisplayName("File is found and formatted using defined root")
    void testFileWithRoot() throws Exception {
        Assumptions.assumeTrue(new File("../../../github/jamal/README.adoc").exists());
        TestThat.theInput("{@define root=../../../}{@file github/jamal/README.adoc}").results("github/jamal/README.adoc");
    }

    @Test
    @DisplayName("File macro throws exception if file does not exist")
    void testFileThrowsNonExistent() throws Exception {
        TestThat.theInput("{@file abraka/dabra/cicadas/bite/my/donkey}").throwsBadSyntax();
    }

    @Test
    @DisplayName("File macro throws exception if file exists biut is a directory")
    void testFileThrowsForDirectory() throws Exception {
        TestThat.theInput("{@file ..}").throwsBadSyntax();
    }
}
