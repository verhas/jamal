package javax0.jamal.test.tools.params;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.Params;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestParams {

    @Test
    @DisplayName("Parsing simple parameters, string and integer")
    void testSimpleParameters() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin").asInt();
        final var top = Params.<Integer>holder("top").asInt();
        final var left = Params.<String>holder("left");
        Params.using(processor).keys(margin, top, left).parse(Input.makeInput("margin=2 top=3 left=\"aligned\""));
        Assertions.assertEquals(2, (int) margin.get());
        Assertions.assertEquals(3, (int) top.get());
        Assertions.assertEquals("aligned", left.get());
    }

    @Test
    @DisplayName("Parsing simple parameters, string and integer")
    void testSimpleUnquotedParameters() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var left = Params.<String>holder("left");
        Params.using(processor).keys(left).parse(Input.makeInput("left=aligned"));
        Assertions.assertEquals("aligned", left.get());
    }

    @Test
    @DisplayName("Parsing simple parameters just present without value")
    void testSimpleParameterJustPresent() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var left = Params.<Boolean>holder("left").asBoolean();
        final var right = Params.<Boolean>holder("right").asBoolean();
        Params.using(processor).keys(left, right).parse(Input.makeInput("left"));
        Assertions.assertTrue(left.get());
        Assertions.assertFalse(right.get());
    }

    @Test
    @DisplayName("Parsing parameters and one comes from user defined macro")
    void testSimpleParametersOneFromUDMacro() throws BadSyntax {
        final var processor = new Processor("{", "}");
        processor.process(Input.makeInput("{@define margin=2}"));
        final var margin = Params.<Integer>holder("margin").asInt();
        final var top = Params.<Integer>holder("top").asInt();
        final var left = Params.<String>holder("left");
        Params.using(processor).keys(margin, top, left).parse(Input.makeInput("top=3 left=\"aligned\""));
        Assertions.assertEquals(2, (int) margin.get());
        Assertions.assertEquals(3, (int) top.get());
        Assertions.assertEquals("aligned", left.get());
    }

    @Test
    @DisplayName("Parsing parameters and one comes from user defined macro")
    void testSimpleParametersOneFromUDMacroOverride() throws BadSyntax {
        final var processor = new Processor("{", "}");
        processor.process(Input.makeInput("{@define margin=3}"));
        final var margin = Params.<Integer>holder("margin").asInt();
        Params.using(processor).keys(margin).parse(Input.makeInput("margin=2"));
        Assertions.assertEquals(2, (int) margin.get());
    }

    @Test
    @DisplayName("It is not a problem when a parameter is missing")
    void testMissingParameter() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin").asInt();
        final var missing = Params.<String>holder("missing");
        Params.using(processor).keys(margin, missing).parse(Input.makeInput("margin=2"));
        Assertions.assertEquals(2, (int) margin.get());
    }

    @Test
    @DisplayName("Line can be continued with trailing \\")
    void testContinuationLine() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin").asInt();
        final var top = Params.<Integer>holder("top").asInt();
        final var left = Params.<String>holder("left");
        Params.using(processor).keys(margin, top, left).parse(Input.makeInput("margin=2 top=3 \\\n      left=\"aligned\""));
        Assertions.assertEquals(2, (int) margin.get());
        Assertions.assertEquals(3, (int) top.get());
        Assertions.assertEquals("aligned", left.get());
    }

    @Test
    @DisplayName("A multi-line can present on a single line")
    void testMultiLineString() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var left = Params.<String>holder("left");
        Params.using(processor).keys(left).parse(Input.makeInput("left=\"\"\"aligned\"\"\""));
        Assertions.assertEquals("aligned", left.get());
    }

    @Test
    @DisplayName("A multi-line can present on a multi line")
    void testMultiLineStringML() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var left = Params.<String>holder("left");
        Params.using(processor).keys(left).parse(Input.makeInput("left=\"\"\"alig\nned\"\"\""));
        Assertions.assertEquals("alig\nned", left.get());
    }

    @Test
    @DisplayName("single-valued parameter returns in a list")
    void testMultiValuedParameterSingleValue() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var left = Params.<List<?>>holder("left").asList();
        Params.using(processor).keys(left).parse(Input.makeInput("left=\"aligned\""));
        Assertions.assertEquals("aligned", left.get().get(0));
    }

    @Test
    @DisplayName("A multi-valued parameter returns in a list")
    void testMultiValuedParameterMultiValue() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var left = Params.<List<?>>holder("left").asList();
        Params.using(processor).keys(left).parse(Input.makeInput("left=\"aligned\"left=\"alignad\""));
        Assertions.assertEquals("aligned", left.get().get(0));
        Assertions.assertEquals("alignad", left.get().get(1));
    }

    @Test
    @DisplayName("Non-present parameter returns empty List")
    void testNonPresentEmptyList() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var left = Params.<List<?>>holder("left").asList();
        Params.using(processor).keys(left).parse(Input.makeInput(""));
        Assertions.assertTrue(left.get().isEmpty());
    }

    @Test
    @DisplayName("UD macro defined value is also returned in a list")
    void testListParametersOneFromUDMacro() throws BadSyntax {
        final var processor = new Processor("{", "}");
        processor.process(Input.makeInput("{@define margin=2}"));
        final var margin = Params.<List<?>>holder("margin").asList();
        Params.using(processor).keys(margin).parse(Input.makeInput(""));
        Assertions.assertEquals("2", margin.get().get(0));
    }

    @Test
    @DisplayName("UD macro value is not considered for boolean value")
    void testNoUDMacroForBoolean() throws BadSyntax {
        final var processor = new Processor("{", "}");
        processor.process(Input.makeInput("{@define margin=true}"));
        final var margin = Params.<Boolean>holder("margin").asBoolean();
        Params.using(processor).keys(margin).parse(Input.makeInput(""));
        Assertions.assertFalse(margin.get());
    }
    /* -------------------------------------------------------------------------------------------------------------- */

    @Test
    @DisplayName("Throws up for forbidden parameter while parsing")
    void testUnusedParameter() {
        final var processor = new Processor("{", "}");
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).keys().parse(Input.makeInput("margin")));
    }

    @Test
    @DisplayName("Throws up for unterminated string")
    void testUnterminatedString() {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin");
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).keys(margin).parse(Input.makeInput("margin=\"")));
    }

    @Test
    @DisplayName("Throws up for unterminated multi-line string")
    void testUnterminatedMLString() {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin").asInt();
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).keys(margin).parse(Input.makeInput("margin=\"\"\"")));
    }

    @Test
    @DisplayName("Throws up for single line string in multi-lines")
    void testUnterminatedLineString() {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin").asInt();
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).keys(margin).parse(Input.makeInput("margin=\"\n\"")));
    }

    @Test
    @DisplayName("Throws up on unquoted empty string")
    void testUnquotedEmptyString() {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin").asInt();
        final var left = Params.<String>holder("left");
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).keys(margin, left).parse(Input.makeInput("margin= left=5")));
    }

    @Test
    @DisplayName("Handles unquoted empty string at the last position")
    void testUnquotedEmptyStringLast() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin").asInt();
        final var left = Params.<String>holder("left");
        Params.using(processor).keys(margin, left).parse(Input.makeInput("margin=5 left="));
        Assertions.assertEquals("", left.get());
    }

}

