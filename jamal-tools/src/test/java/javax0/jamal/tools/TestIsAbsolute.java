package javax0.jamal.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestIsAbsolute {


    @DisplayName("Test different absolute file names")
    @Test
    void testAbsoluteFileNames() {
        Assertions.assertTrue(FileTools.isAbsolute("/home/javax0"));
        Assertions.assertTrue(FileTools.isAbsolute("C:\\Program Files\\Java"));
        Assertions.assertTrue(FileTools.isAbsolute("https://github.com/verhas/jamal"));
        Assertions.assertTrue(FileTools.isAbsolute("~/.m2/settings.xml"));
    }

    @DisplayName("Test different relative file names")
    @Test
    void testRelativeFileNames() {
        Assertions.assertFalse(FileTools.isAbsolute("./home/javax0"));
        Assertions.assertFalse(FileTools.isAbsolute("../home/javax0"));
        Assertions.assertFalse(FileTools.isAbsolute("home/javax0"));
        Assertions.assertFalse(FileTools.isAbsolute("home\\javax0"));
    }
}
