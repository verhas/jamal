package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class TestRegex {

    @Test
    @DisplayName("replaceAll works with regular expressions")
    void simpleReplaceAll() throws InvocationTargetException, NoSuchMethodException, InstantiationException, BadSyntax, IllegalAccessException {
        var replaceAll = TestThat.theMacro(Regex.ReplaceAll.class);
        replaceAll.fromTheInput("/this is the start/\\s*/").results("thisisthestart");
        replaceAll.fromTheInput("/apple baum wolle/(\\w)\\1/$1").results("aple baum wole");
    }
}
