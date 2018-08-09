package javax0.jamal.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestProcessor {

    @Test
    @DisplayName("fetches the first macro without any inner macros")
    public void getFirstMacro() {
        final var input = new StringBuilder("this is the body of the macro}");
        final var sut = new Processor("{", "}");
        final var result = sut.getNextMacroBody(input);
        Assertions.assertEquals("this is the body of the macro", result);
    }

    @Test
    @DisplayName("fetches the first macro with inner macros")
    public void getNestedMacro() {
        final var input = new StringBuilder("this is the {body} of the macro}");
        final var sut = new Processor("{", "}");
        final var result = sut.getNextMacroBody(input);
        Assertions.assertEquals("this is the {body} of the macro", result);
    }

    @Test
    @DisplayName("fetches the second on the second call macro with inner macros")
    public void getSecondMacro() {
        final var input = new StringBuilder("this is the {body} of the macro}{this is the second macro}");
        final var sut = new Processor("{", "}");
        sut.getNextMacroBody(input);
        input.delete(0,1);
        final var result = sut.getNextMacroBody(input);
        Assertions.assertEquals("this is the second macro", result);
    }

}
