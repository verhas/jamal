package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestOptions {

    @Test
    void testOptions()throws Exception{
        TestThat.theInput(
            "{@define macro(a,b,c)=a is a, b is b{#if :c:, and c is c}}\n" +
                "{macro :apple:pie:}{@comment here we need : at end, default is not lenient}\n" +
                "{#ident {@options lenient}{macro :apple:pie}}{@comment options is local inside the ident block}\n" +
                "{macro :apple:pie:}{@comment here we must have the trailing : because options is local}\n" +
                "{#ident\n" +
                "{#ident {@options lenient}{macro :apple:pie}{@export `options}}{@comment local but gets exported one level up}\n" +
                "{macro :apple:pie}}\n" +
                "{macro :apple:pie:}{@comment was not exported to this level, only to inside the outer ident block}\n" +
                "{@options lenient}{@comment now this is on the global level}\n" +
                "{macro :apple:pie}{@comment nice and easy, global}\n" +
                "{@options ~lenient}{@comment and we can switch it off}\n" +
                "{macro :apple:pie:}\n" +
                "{@options any|option|can  | go | ~go | no go}"
        ).results(
            "\n" +
                "apple is apple, pie is pie\n" +
                "apple is apple, pie is pie\n" +
                "apple is apple, pie is pie\n" +
                "apple is apple, pie is pie\n" +
                "apple is apple, pie is pie\n" +
                "apple is apple, pie is pie\n" +
                "\n" +
                "apple is apple, pie is pie\n" +
                "\n" +
                "apple is apple, pie is pie\n"
        );
    }

    @Test
    void testNewLineOptions()throws Exception{
        TestThat.theInput(
            "By default every character is important and no new line characters are eaten up.\n" +
                "Therefore the following to lines will be like: \"alma \\n alma \\n \"\n" +
                "{@define z=alma}{z}\n" +
                "{z}\n" +
                "When we define the {@sep/(/)}{@options nl}(@sep) then the new line character after the macro closing character is skipped.\n" +
                "This means that here we will have an empty line and after that the next line will be \"almaalma\" and then \"alma\"\n" +
                "without an empty line between\n" +
                "{@options ~nl}\n" +
                "{z}\\\n" +
                "{z}\\\n" +
                "\n" +
                "{z}\\\n" +
                "\n" +
                "See... there is no empty line before this line or before the previous line."
        ).results(
            "By default every character is important and no new line characters are eaten up.\n" +
                "Therefore the following to lines will be like: \"alma \\n alma \\n \"\n" +
                "alma\n" +
                "alma\n" +
                "When we define the {@options nl} then the new line character after the macro closing character is skipped.\n" +
                "This means that here we will have an empty line and after that the next line will be \"almaalma\" and then \"alma\"\n" +
                "without an empty line between\n" +
                "\n" +
                "almaalma\n" +
                "alma\n" +
                "See... there is no empty line before this line or before the previous line."
        );
    }
}
