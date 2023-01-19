package javax0.jamal.java;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestDownload {

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @Test
    void testDownload() throws Exception {
        final var dir = System.getProperty("user.home") + "/.m2/repository/com/squareup/tools/build/maven-archeologist/0.0.3.1/";
        deleteDirectory(new File(dir));
        TestThat.theInput("{@maven:download com.squareup.tools.build:maven-archeologist:0.0.3.1}"
        ).results(
                "");
        Assertions.assertTrue(new File(dir + "maven-archeologist-0.0.3.1.jar").exists());
    }

}
