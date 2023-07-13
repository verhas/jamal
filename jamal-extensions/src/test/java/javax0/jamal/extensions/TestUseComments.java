package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestUseComments {

    @Test
    @DisplayName("Use comments removes comments from the input used on the top level")
    void testTopLevel() throws Exception {
        TestThat.theInput("{@use javax0.jamal.extensions.UseComments}{@useComments}\\\n" +
                "//{@define a=1}{a}{@define a=2}{a}\n" +
                "// this is also a comment\n" +
                "aaa").results("aaa");
    }

    @Test
    @DisplayName("Use comments removes comments from the input used in a macro")
    void testInMacro() throws Exception {
        TestThat.theInput("{@sep🥰 😘}🥰@use javax0.jamal.extensions.UseComments😘\\\n" +
                "🥰@define COMMENTED(😀)=🥰@useComments😘\n😀😘\\\n" +
                "🥰COMMENTED| //whatever it is comment\n" +
                "// this is also comment, because it is inside the macro\n" +
                "😘\\\n"+
                "// this is also a comment, but not removed\n" +
                "aaa").results("// this is also a comment, but not removed\naaa");
    }


    @Test
    @DisplayName("Test the use of emoji characters in Jamal")
    void testEmojiDefine()throws Exception{
        TestThat.theInput("{@define 🐶 =🐕}{\uD83D\uDC36}").results("\uD83D\uDC15");
    }
}
