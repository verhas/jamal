package javax0.jamal;

import javax0.jamal.api.BadSyntax;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

class FormatTest {

    @Test
    @DisplayName("Simple text formatted with macros")
    void testSimple() throws BadSyntax {
        Assertions.assertEquals("Hallo Jacob Hralovek!",Format.format("Hallo {{firstName}} {{familyName}}!", Map.of("firstName","Jacob","familyName","Hralovek")));
    }

}
