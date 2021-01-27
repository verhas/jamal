package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestFilesMacro {

    @Test
    @DisplayName("Directory is found and formatted")
    void testDirectory() throws Exception {
        TestThat.theInput("{@define directoryFormat=$canonicalPath}{#replace {@options regex} |{@directory ./}|^.*?jamal|jamal}").results("jamal/jamal-snippet");
        TestThat.theInput("{@define directoryFormat=$canonicalPath}{#replace {@options regex} |{@directory ..}|^.*?jamal|jamal}").results("jamal");
        TestThat.theInput("{@define root=../../../}{@directory github/jamal}").results("github/jamal");
    }

    @Test
    @DisplayName("Directory is found and formatted using defined root")
    void testDirectoryWithRoot() throws Exception {
        TestThat.theInput("{@define root=../../../}{@directory github/jamal}").results("github/jamal");
    }

    @Test
    @DisplayName("Directory macro throws exception if directory does not exist")
    void testDirectoryThrowsNonExistent() throws Exception {
        TestThat.theInput("{@directory abraka/dabra/cicadas/bite/my/donkey}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Directory macro throws exception if directory does not exist")
    void testDirectoryThrowsOnFile() throws Exception {
        TestThat.theInput("{@directory ./README.adoc}").throwsBadSyntax();
    }

    @Test
    @DisplayName("File is found and formatted")
    void testFile() throws Exception {
        TestThat.theInput("{@define fileFormat=$canonicalPath}{#replace {@options regex} |{@file ./README.adoc}|^.*?jamal|jamal}").results("jamal/jamal-snippet/README.adoc");
        TestThat.theInput("{@define root=../}{@define fileFormat=`$name`}{@file README.adoc}").results("`README.adoc`");
    }

    @Test
    @DisplayName("File is found and formatted using defined root")
    void testFileWithRoot() throws Exception {
        TestThat.theInput("{@define root=../../../}{@file github/jamal/README.adoc}").results("github/jamal/README.adoc");
    }

    @Test
    @DisplayName("File macro throws exception if directory does not exist")
    void testFileThrowsNonExistent() throws Exception {
        TestThat.theInput("{@directory abraka/dabra/cicadas/bite/my/donkey}").throwsBadSyntax();
    }
}
