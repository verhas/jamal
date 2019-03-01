package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestProcessor {

    @Test
    @DisplayName("fetches the first macro without any inner macros")
    public void getFirstMacro() throws BadSyntaxAt {
        final var input = new Input("this is the body of the macro}");
        final var sut = new Processor("{", "}");
        final var result = sut.getNextMacroBody(input);
        Assertions.assertEquals("this is the body of the macro", result);
    }

    @Test
    @DisplayName("throws exception when the macro is not terminated")
    public void unterminated() throws BadSyntaxAt {
        final var input = new Input("this is the body of the macro unterminated");
        final var sut = new Processor("{", "}");
        Assertions.assertThrows(BadSyntaxAt.class, () -> sut.getNextMacroBody(input));
    }

    @Test
    @DisplayName("fetches the first macro with inner macros")
    public void getNestedMacro() throws BadSyntaxAt {
        final var input = new Input("this is the {body} of the macro}");
        final var sut = new Processor("{", "}");
        final var result = sut.getNextMacroBody(input);
        Assertions.assertEquals("this is the {body} of the macro", result);
    }

    @Test
    @DisplayName("fetches the second on the second call macro with inner macros")
    public void getSecondMacro() throws BadSyntaxAt {
        final var input = new Input("this is the {body} of the macro}{this is the second macro}");
        final var sut = new Processor("{", "}");
        sut.getNextMacroBody(input);
        input.delete(0, 1);
        final var result = sut.getNextMacroBody(input);
        Assertions.assertEquals("this is the second macro", result);
    }

    @Test
    @DisplayName("a simple define with arguments")
    public void simpleDefine() throws BadSyntaxAt, BadSyntax {
        final var input = new Input("{@define q=zqqz}{q}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("zqqz", result);
    }

    @Test
    @DisplayName("a simple define with arguments")
    public void testSimpleDefineWargs() throws BadSyntaxAt, BadSyntax {
        final var input = new Input("{@define q(a,b)=abba}{q/z/q}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("zqqz", result);
    }

    @Test
    @DisplayName("nested macro definitions")
    public void testNestedDefine() throws BadSyntaxAt, BadSyntax {
        final var input = new Input("{#define b=b}{#define q=zqq{#define b=z}{b}}{q}{b}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("zqqzb", result);
    }

    @Test
    @DisplayName("nested macro definitions with late eval")
    public void testNestedDefineLater() throws BadSyntaxAt, BadSyntax {
        final var input = new Input("{#define b=b}{@define q=zqq{#define b=z}{b}}{q}{b}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("zqqzz", result);
    }

    @Test
    @DisplayName("nested macro definitions with late eval verbatim")
    public void testNestedDefineLateVerbatim() throws BadSyntaxAt, BadSyntax {
        final var input = new Input("{#define b=b}{@define q=zqq{}{b}}{@verbatim q}{b}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("zqq{}{b}b", result);
    }

    @Test
    @DisplayName("nested macro definitions with late eval verbatim")
    public void testComment() throws BadSyntaxAt, BadSyntax {
        final var input = new Input("{#define b=b}{@comment {@define q=zqq{}{b}}}{b}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("b", result);
    }


    @Test
    @DisplayName("testsupport verbatim protected user defined macro")
    public void testVerbatimUdMacroUse() throws BadSyntaxAt, BadSyntax {
        final var input = new Input("{@define b={zz}}{@verbatim b}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("{zz}", result);
    }
}
