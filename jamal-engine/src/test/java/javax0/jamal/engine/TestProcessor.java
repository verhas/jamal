package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Context;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Macro;
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
    public void unterminated() {
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
        input.delete(1);
        final var result = sut.getNextMacroBody(input);
        Assertions.assertEquals("this is the second macro", result);
    }

    @Test
    @DisplayName("a simple define with arguments")
    public void simpleDefine() throws BadSyntax {
        final var input = new Input("{@define q=zqqz}{q}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("zqqz", result);
    }

    @Test
    @DisplayName("a simple define with arguments")
    public void testSimpleDefineWargs() throws BadSyntax {
        final var input = new Input("{@define q(a,b)=abba}{q/z/q}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("zqqz", result);
    }

    @Test
    @DisplayName("nested macro definitions")
    public void testNestedDefine() throws BadSyntax {
        final var input = new Input("{#define b=b}{#define q=zqq{#define b=z}{b}}{q}{b}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("zqqzb", result);
    }

    @Test
    @DisplayName("nested macro definitions with late eval")
    public void testNestedDefineLater() throws BadSyntax {
        final var input = new Input("{#define b=b}{@define q=zqq{#define b=z}{b}}{q}{b}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("zqqzz", result);
    }

    @Test
    @DisplayName("nested macro definitions with late eval verbatim")
    public void testNestedDefineLateVerbatim() throws BadSyntax {
        final var input = new Input("{#define b=b}{@define q=zqq{}{b}}{@verbatim q}{b}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("zqq{}{b}b", result);
    }

    @Test
    @DisplayName("nested macro definitions with late eval verbatim")
    public void testComment() throws BadSyntax {
        final var input = new Input("{#define b=b}{@comment {@define q=zqq{}{b}}}{b}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("b", result);
    }

    @Test
    @DisplayName("testsupport verbatim protected user defined macro")
    public void testVerbatimUdMacroUse() throws BadSyntax {
        final var input = new Input("{@define b={zz}}{@verbatim b}");
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals("{zz}", result);
    }

    public static class SignalMacro implements Macro {
        String closed = "not closed";

        @Override
        public String evaluate(javax0.jamal.api.Input in, javax0.jamal.api.Processor processor) throws BadSyntax {
            return closed;
        }
    }

    public static class AutoClosingMacro implements Macro, AutoCloseable {

        javax0.jamal.api.Processor p = null;

        @Override
        public void close() throws Exception {
            SignalMacro sm = (SignalMacro) p.getRegister().getMacro("signalmacro").get();
            sm.closed = "closed";
        }

        @Override
        public String evaluate(javax0.jamal.api.Input in, javax0.jamal.api.Processor processor) throws BadSyntax {
            p = processor;
            return "AutoClosing";
        }
    }

    @Test
    @DisplayName("Macro implementing AutoClose gets closed when gets out of scope")
    public void testAutoClose() throws BadSyntax {
        final var input = new Input(
            "{@use javax0.jamal.engine.TestProcessor.SignalMacro}" +
                "{@signalmacro}\n" +
                "{#ident" +
                "  {@use javax0.jamal.engine.TestProcessor.AutoClosingMacro}" +
                "{@autoclosingmacro}\n" +
                "{@signalmacro}" +
                "}\n" +
                "{@signalmacro}\n"
        );
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals(
            "not closed\n" +
                "AutoClosing\n" +
                "not closed\n" +
                "closed\n", result);
    }


    public static class AutoClosingUdMacro implements Identified, AutoCloseable {

        final javax0.jamal.api.Processor processor;

        AutoClosingUdMacro(javax0.jamal.api.Processor processor) {
            this.processor = processor;
        }

        @Override
        public void close() throws Exception {
            SignalMacro sm = (SignalMacro) processor.getRegister().getMacro("signalmacro").get();
            sm.closed = "closed";
        }

        @Override
        public String getId() {
            return "wtf";
        }
    }

    public static class UdReg implements Macro {

        @Override
        public String evaluate(javax0.jamal.api.Input in, javax0.jamal.api.Processor processor) throws BadSyntax {
            processor.getRegister().define(new AutoClosingUdMacro(processor));
            return "";
        }
    }

    @Test
    @DisplayName("User Defined Macro implementing AutoClose gets closed when gets out of scope")
    public void testAutoCloseUdm() throws BadSyntax {
        final var input = new Input(
            "{@use javax0.jamal.engine.TestProcessor.SignalMacro}" +
                "{@signalmacro}\n" +
                "{#ident" +
                "  {@use javax0.jamal.engine.TestProcessor.UdReg}" +
                "{@udreg}\n" +
                "{@signalmacro}" +
                "}\n" +
                "{@signalmacro}\n"
        );
        final var sut = new Processor("{", "}");
        final var result = sut.process(input);
        Assertions.assertEquals(
            "not closed\n" +
                "not closed\n" +
                "closed\n", result);
    }


    public static class Deferred implements AutoCloseable {

        private final javax0.jamal.api.Processor processor;

        public Deferred(javax0.jamal.api.Processor processor) {
            this.processor = processor;
        }

        public boolean isClosed = false;

        @Override
        public void close() throws Exception {
            SignalMacro sm = (SignalMacro) processor.getRegister().getMacro("signalmacro").get();
            sm.closed = "closed";
            isClosed = true;
        }
    }

    public static class DeferredClosingMacro implements Macro {


        @Override
        public String evaluate(javax0.jamal.api.Input in, javax0.jamal.api.Processor processor) throws BadSyntax {
            MyContext ctx = (MyContext) processor.getContext();
            ctx.deferred = new Deferred(processor);
            processor.deferredClose(ctx.deferred);
            return "AutoClosing";
        }
    }

    public static class MyContext implements Context {
        Deferred deferred;
    }

    @Test
    @DisplayName("Macro implementing AutoClose gets closed when gets out of scope")
    public void testDeferredClose() throws Exception {
        final var input = new Input(
            "{@use javax0.jamal.engine.TestProcessor.SignalMacro}" +
                "{@signalmacro}\n" +
                "{#ident" +
                "  {@use javax0.jamal.engine.TestProcessor.DeferredClosingMacro}" +
                "{@deferredclosingmacro}\n" +
                "{@signalmacro}" +
                "}\n" +
                "{@signalmacro}\n"
        );
        MyContext ctx = new MyContext();
        try (final var sut = new Processor("{", "}", ctx)) {
            final var result = sut.process(input);
            Assertions.assertEquals(
                "not closed\n" +
                    "AutoClosing\n" +
                    "not closed\n" +
                    "not closed\n", result);
            Assertions.assertFalse(ctx.deferred.isClosed);
        }
        Assertions.assertTrue(ctx.deferred.isClosed);
    }

}
