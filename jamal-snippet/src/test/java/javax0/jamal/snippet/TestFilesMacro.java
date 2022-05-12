package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static javax0.jamal.DocumentConverter.getRoot;

public class TestFilesMacro {

    @Test
    @DisplayName("Directory is found and formatted")
    void testDirectory() throws Exception {
        final var root = getRoot() + "/jamal-snippet/src/main/java/javax0/jamal/snippet/";
        TestThat.theInput("{@define directoryFormat=$canonicalPath}{#replace {@options regex} |{@directory ./}|^.*?main|main}")
                .atPosition(root, 0, 0)
                .matches("main.java.javax0.jamal.snippet");
        TestThat.theInput("{@define directoryFormat=$canonicalPath}{#replace {@options regex} |{@directory ..}|^.*?main|main}")
                .atPosition(root, 0, 0)
                .matches("main.java.javax0.jamal");
        TestThat.theInput("{@define root=../../../}{@directory javax0/jamal}")
                .atPosition(root, 0, 0)
                .matches("javax0.jamal");
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
        TestThat.theInput("{@define fileFormat=$canonicalPath}{#replace {@options regex} |{@file ./README.adoc}|^.*?jamal-|jamal-}").matches("jamal-snippet.README.adoc");
        TestThat.theInput("{@define root=../}{@define fileFormat=`$name`}{@file README.adoc}").results("`README.adoc`");
    }

    @Test
    @DisplayName("File is found and extension cutting off works")
    void testFileExtensions() throws Exception {
        TestThat.theInput("{@file (format=\"$bareNaked\")./src/test/resources/fileName}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked1\")./src/test/resources/fileName}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked2\")./src/test/resources/fileName}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked3\")./src/test/resources/fileName}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked4\")./src/test/resources/fileName}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked5\")./src/test/resources/fileName}").results("fileName");

        TestThat.theInput("{@file (format=\"$bareNaked\")./src/test/resources/fileName.ext}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked1\")./src/test/resources/fileName.ext}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked2\")./src/test/resources/fileName.ext}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked3\")./src/test/resources/fileName.ext}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked4\")./src/test/resources/fileName.ext}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked5\")./src/test/resources/fileName.ext}").results("fileName");

        TestThat.theInput("{@file (format=\"$bareNaked\")./src/test/resources/fileName.ext1.ext2}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked1\")./src/test/resources/fileName.ext1.ext2}").results("fileName.ext1");
        TestThat.theInput("{@file (format=\"$naked2\")./src/test/resources/fileName.ext1.ext2}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked3\")./src/test/resources/fileName.ext1.ext2}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked4\")./src/test/resources/fileName.ext1.ext2}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked5\")./src/test/resources/fileName.ext1.ext2}").results("fileName");

        TestThat.theInput("{@file (format=\"$bareNaked\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("fileName");
        TestThat.theInput("{@file (format=\"$naked1\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("fileName.ext1.ext2.ext3.ext4.ext5");
        TestThat.theInput("{@file (format=\"$naked2\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("fileName.ext1.ext2.ext3.ext4");
        TestThat.theInput("{@file (format=\"$naked3\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("fileName.ext1.ext2.ext3");
        TestThat.theInput("{@file (format=\"$naked4\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("fileName.ext1.ext2");
        TestThat.theInput("{@file (format=\"$naked5\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("fileName.ext1");


        TestThat.theInput("{@file (format=\"$extensions\")./src/test/resources/fileName}").results("");
        TestThat.theInput("{@file (format=\"$extension1\")./src/test/resources/fileName}").results("");
        TestThat.theInput("{@file (format=\"$extension2\")./src/test/resources/fileName}").results("");
        TestThat.theInput("{@file (format=\"$extension3\")./src/test/resources/fileName}").results("");
        TestThat.theInput("{@file (format=\"$extension4\")./src/test/resources/fileName}").results("");
        TestThat.theInput("{@file (format=\"$extension5\")./src/test/resources/fileName}").results("");

        TestThat.theInput("{@file (format=\"$extensions\")./src/test/resources/fileName.ext}").results("ext");
        TestThat.theInput("{@file (format=\"$extension1\")./src/test/resources/fileName.ext}").results("ext");
        TestThat.theInput("{@file (format=\"$extension2\")./src/test/resources/fileName.ext}").results("ext");
        TestThat.theInput("{@file (format=\"$extension3\")./src/test/resources/fileName.ext}").results("ext");
        TestThat.theInput("{@file (format=\"$extension4\")./src/test/resources/fileName.ext}").results("ext");
        TestThat.theInput("{@file (format=\"$extension5\")./src/test/resources/fileName.ext}").results("ext");

        TestThat.theInput("{@file (format=\"$extensions\")./src/test/resources/fileName.ext1.ext2}").results("ext1.ext2");
        TestThat.theInput("{@file (format=\"$extension1\")./src/test/resources/fileName.ext1.ext2}").results("ext2");
        TestThat.theInput("{@file (format=\"$extension2\")./src/test/resources/fileName.ext1.ext2}").results("ext1.ext2");
        TestThat.theInput("{@file (format=\"$extension3\")./src/test/resources/fileName.ext1.ext2}").results("ext1.ext2");
        TestThat.theInput("{@file (format=\"$extension4\")./src/test/resources/fileName.ext1.ext2}").results("ext1.ext2");
        TestThat.theInput("{@file (format=\"$extension5\")./src/test/resources/fileName.ext1.ext2}").results("ext1.ext2");

        TestThat.theInput("{@file (format=\"$extensions\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("ext1.ext2.ext3.ext4.ext5.ext6");
        TestThat.theInput("{@file (format=\"$extension1\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("ext6");
        TestThat.theInput("{@file (format=\"$extension2\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("ext5.ext6");
        TestThat.theInput("{@file (format=\"$extension3\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("ext4.ext5.ext6");
        TestThat.theInput("{@file (format=\"$extension4\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("ext3.ext4.ext5.ext6");
        TestThat.theInput("{@file (format=\"$extension5\")./src/test/resources/fileName.ext1.ext2.ext3.ext4.ext5.ext6}").results("ext2.ext3.ext4.ext5.ext6");
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
