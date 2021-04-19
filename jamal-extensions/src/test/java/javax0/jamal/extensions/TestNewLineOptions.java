package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class TestNewLineOptions {

    @Test
    @DisplayName("When options 'nl' is used new lines following macro closing is skipped")
    void testNewLineSkipping() throws InvocationTargetException, NoSuchMethodException, InstantiationException, BadSyntax, IllegalAccessException {
        TestThat.theInput(
            "{@options nl}By default every character is important and no new line characters are eaten up.\n" +
                "Therefore the following to lines will be like: \"alma \\n alma \\n \"\n" +
                "{@define z=alma}{z}\\\n" +
                "{z}\n" +
                "When we define the {@sep/(/)}{@options nl}(@sep) then the new line character after the macro closing string is skipped.\n" +
                "This means that here we will have an empty line and after that the next line will be \"almaalma\" and then \"alma\"\n" +
                "without an empty line between\n" +
                "{@options ~nl}\n" +
                "{z}\\      \n" + // here we test that there are spaces before the new-line... in the sample file editor truncates the line removing these spaces
                "{z}\\\n" +
                "\n" +
                "{z}\\\n" +
                "\n" +
                "See... there is no empty line before this line or before the previous line.")
            .results(
                "By default every character is important and no new line characters are eaten up.\n" +
                    "Therefore the following to lines will be like: \"alma \\n alma \\n \"\n" +
                    "alma\\\n" +
                    "alma\n" +
                    "When we define the {@options nl} then the new line character after the macro closing string is skipped.\n" +
                    "This means that here we will have an empty line and after that the next line will be \"almaalma\" and then \"alma\"\n" +
                    "without an empty line between\n" +
                    "\n" +
                    "almaalma\n" +
                    "alma\n" +
                    "See... there is no empty line before this line or before the previous line.");
    }

}
