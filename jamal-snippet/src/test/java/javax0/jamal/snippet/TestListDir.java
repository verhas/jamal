package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;

public class TestListDir {

    //@Test
    void testListDir() throws Exception{
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
            "time          = $time\n" +
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
            "time          = 2021-01-08T10:28:50.554207323Z\n" +
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
            "time          = 2021-01-08T10:28:50.543442524Z\n" +
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
            "time          = 2021-01-08T10:28:50.559429Z\n" +
            "\n");
    }
}

