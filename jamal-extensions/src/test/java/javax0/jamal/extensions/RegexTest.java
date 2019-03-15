package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class RegexTest {

    @Test
    @DisplayName("replaceAll works with regular expressions")
    void simpleReplaceAll() throws InvocationTargetException, NoSuchMethodException, InstantiationException, BadSyntax, IllegalAccessException {
        var replaceAll = TestThat.forMacro(Regex.ReplaceAll.class);
        replaceAll.fromInput("/this is the start/\\s*/").results("thisisthestart");
        replaceAll.fromInput("/apple baum wolle/(\\w)\\1/$1").results("aple baum wole");
    }
}
