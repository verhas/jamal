package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
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
        input.delete(0, 1);
        final var result = sut.getNextMacroBody(input);
        Assertions.assertEquals("this is the second macro", result);
    }

    @Test
    @DisplayName("a simple define with arguments")
    public void simpleDefine() throws BadSyntax {
        final var input = "{@define q=zqqz}{q}";
        final var sut = new Processor("{", "}");
        final var result = sut.process(new Input(new StringBuilder(input),null));
        Assertions.assertEquals("zqqz", result);
    }

    @Test
    @DisplayName("a simple define with arguments")
    public void testSimpleDefineWargs() throws BadSyntax {
        final var input = "{@define q(a,b)=abba}{q/z/q}";
        final var sut = new Processor("{", "}");
        final var result = sut.process(new Input(new StringBuilder(input),null));
        Assertions.assertEquals("zqqz", result);
    }

    @Test
    @DisplayName("nested macro definitions")
    public void testNestedDefine() throws BadSyntax {
        final var input = "{#define b=b}{#define q=zqq{#define b=z}{b}}{q}{b}";
        final var sut = new Processor("{", "}");
        final var result = sut.process(new Input(new StringBuilder(input),null));
        Assertions.assertEquals("zqqzb", result);
    }
    @Test
    @DisplayName("nested macro definitions with late eval")
    public void testNestedDefineLater() throws BadSyntax {
        final var input = "{#define b=b}{@define q=zqq{#define b=z}{b}}{q}{b}";
        final var sut = new Processor("{", "}");
        final var result = sut.process(new Input(new StringBuilder(input),null));
        Assertions.assertEquals("zqqzz", result);
    }

    @Test
    @DisplayName("nested macro definitions with late eval verbatim")
    public void testNestedDefineLateVerbatim() throws BadSyntax {
        final var input = "{#define b=b}{@define q=zqq{}{b}}{@verbatim q}{b}";
        final var sut = new Processor("{", "}");
        final var result = sut.process(new Input(new StringBuilder(input),null));
        Assertions.assertEquals("zqq{}{b}b", result);
    }
}
