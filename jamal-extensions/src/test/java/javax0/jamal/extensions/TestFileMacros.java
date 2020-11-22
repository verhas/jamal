package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class TestFileMacros {


    @Test
    void testWriteFileAndThenRead() throws InvocationTargetException, NoSuchMethodException, InstantiationException, BadSyntax, IllegalAccessException {
        TestThat.theInput(
            "{@import res:FileMacros.jim}" +
                "{@write my_tempFile.txt will contain this}" +
                "{@read my_tempFile.txt}"
        ).atPosition("target/1.txt", 1, 1)
            .results("will contain this");
    }
}
