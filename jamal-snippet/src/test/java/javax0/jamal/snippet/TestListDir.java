package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestListDir {

    @Test
    void testListDir() throws Exception {
        TestThat.theInput("{#for ($absolutePath,$name,$simpleName,$isDirectory,$isFile,$isHidden,$canExecute,$canRead,$canWrite,$size,$time) in ({#listDir src/" +
            "{@define maxDepth=1}" +
            "{@define format=$absolutePath|$name|$simpleName|$isDirectory|$isFile|$isHidden|$canExecute|$canRead|$canWrite|$size|$time}})=" +
            "name          = $name\n" +
            "simpleName    = $simpleName\n" +
            "isDirectory   = $isDirectory\n" +
            "isFile        = $isFile\n" +
            "isHidden      = $isHidden\n" +
            "canExecute    = $canExecute\n" +
            "canRead       = $canRead\n" +
            "canWrite      = $canWrite\n" +
            "size          = $size\n" +
            "\n" +
            "}").results("name          = src\n" +
            "simpleName    = src\n" +
            "isDirectory   = true\n" +
            "isFile        = false\n" +
            "isHidden      = false\n" +
            "canExecute    = true\n" +
            "canRead       = true\n" +
            "canWrite      = true\n" +
            "size          = 128\n" +
            "\n" +
            "name          = src/test\n" +
            "simpleName    = test\n" +
            "isDirectory   = true\n" +
            "isFile        = false\n" +
            "isHidden      = false\n" +
            "canExecute    = true\n" +
            "canRead       = true\n" +
            "canWrite      = true\n" +
            "size          = 128\n" +
            "\n" +
            "name          = src/main\n" +
            "simpleName    = main\n" +
            "isDirectory   = true\n" +
            "isFile        = false\n" +
            "isHidden      = false\n" +
            "canExecute    = true\n" +
            "canRead       = true\n" +
            "canWrite      = true\n" +
            "size          = 128\n" +
            "\n");
    }

    @Test
    void testListDirWithOptions() throws Exception {
        TestThat.theInput("{#for ($absolutePath,$name,$simpleName,$isDirectory,$isFile,$isHidden,$canExecute,$canRead,$canWrite,$size,$time) in " +
            "({#listDir (maxDepth=1 format=$absolutePath|$name|$simpleName|$isDirectory|$isFile|$isHidden|$canExecute|$canRead|$canWrite|$size|$time) src/})=" +
            "name          = $name\n" +
            "simpleName    = $simpleName\n" +
            "isDirectory   = $isDirectory\n" +
            "isFile        = $isFile\n" +
            "isHidden      = $isHidden\n" +
            "canExecute    = $canExecute\n" +
            "canRead       = $canRead\n" +
            "canWrite      = $canWrite\n" +
            "size          = $size\n" +
            "\n" +
            "}").results("name          = src\n" +
            "simpleName    = src\n" +
            "isDirectory   = true\n" +
            "isFile        = false\n" +
            "isHidden      = false\n" +
            "canExecute    = true\n" +
            "canRead       = true\n" +
            "canWrite      = true\n" +
            "size          = 128\n" +
            "\n" +
            "name          = src/test\n" +
            "simpleName    = test\n" +
            "isDirectory   = true\n" +
            "isFile        = false\n" +
            "isHidden      = false\n" +
            "canExecute    = true\n" +
            "canRead       = true\n" +
            "canWrite      = true\n" +
            "size          = 128\n" +
            "\n" +
            "name          = src/main\n" +
            "simpleName    = main\n" +
            "isDirectory   = true\n" +
            "isFile        = false\n" +
            "isHidden      = false\n" +
            "canExecute    = true\n" +
            "canRead       = true\n" +
            "canWrite      = true\n" +
            "size          = 128\n" +
            "\n");
    }
}

