package javax0.jamal;

import javax0.jamal.api.BadSyntax;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class FormatterTest {

    @Test
    @DisplayName("Simple text formatted with macros")
    void testSimple() throws BadSyntax {
        Assertions.assertEquals("Hallo Jacob Hralovek!",
            new Formatter("{{","}}").format("Hallo {{firstName}} {{familyName}}!",
                Map.of("firstName", "Jacob", "familyName", "Hralovek")));
    }

    @Test
    @DisplayName("Simple text formatted with macros provided using map")
    void testUsingSimple() throws BadSyntax {
        Assertions.assertEquals("Hallo Jacob Hralovek!",
            new Formatter("{{","}}")
                    .using(Map.of("firstName", "Jacob", "familyName", "Hralovek"))
                    .format("Hallo {{firstName}} {{familyName}}!"))
        ;
    }

    @Test
    @DisplayName("Simple text formatted with macros provided using strings")
    void testUsingStrings() throws BadSyntax {
        Assertions.assertEquals("Hallo Jacob Hralovek!",
                new Formatter("{{","}}")
                        .using("firstName", "Jacob", "familyName", "Hralovek")
                        .format("Hallo {{firstName}} {{familyName}}!"))
        ;
    }

}
