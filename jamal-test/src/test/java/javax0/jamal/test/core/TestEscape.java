package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestEscape {

    @Test
    void escapesGood() throws Exception{
        TestThat.theInput("{@escape `|``|`}").results("");
        TestThat.theInput("{@escape `|`abraka dabra`|`}").results("abraka dabra");
        TestThat.theInput("{@escape`|`abraka dabra`|`}").results("abraka dabra");
        TestThat.theInput("{@escape `|`abraka dabra`|`        }").results("abraka dabra");
        TestThat.theInput("{@escape`|+brrrr`abraka dabra`|+brrrr`}").results("abraka dabra");
    }

    @Test
    void escapesGoodEmbeddedOpenAndClose() throws Exception{
        TestThat.theInput("{@escape `|`{`|`}").results("{");
        TestThat.theInput("{@escape `|`}`|`}").results("}");
        TestThat.theInput("{@escape`|`{ { { }}`|`}").results("{ { { }}");
        TestThat.theInput("{@escape `|`abrak{a} }d{abra`|`        }").results("abrak{a} }d{abra");
        TestThat.theInput("{@escape`|+brrrr`abraka }}dabra`|+brrrr`}").results("abraka }}dabra");
    }

    @Test
    void escapeThrows() throws Exception{
        TestThat.theInput("{@escape `|``|` dddd}").throwsBadSyntax();
        TestThat.theInput("{@escape `|`|`}").throwsBadSyntax();
        TestThat.theInput("{@escape `|`}`|`").throwsBadSyntax();
        TestThat.theInput("{@escape `| sdsds | dddd}").throwsBadSyntax();
        TestThat.theInput("{@escape dfdfdf `|``|`}").throwsBadSyntax();
        TestThat.theInput("{@escape `|`summatirada|}").throwsBadSyntax();
    }
}
