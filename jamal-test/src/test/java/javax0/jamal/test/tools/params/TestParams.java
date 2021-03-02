package javax0.jamal.test.tools.params;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.Params;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class TestParams {


    @Test
    @DisplayName("Parsing simple parameters, string and integer")
    void testSimpleParameters() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("margin", "top", "left")).parse(Input.makeInput("margin=2 top=3 left=\"aligned\""));
        Assertions.assertEquals(2, sut.getInt("margin").getAsInt());
        Assertions.assertEquals(3, sut.getInt("top").getAsInt());
        Assertions.assertEquals("aligned", sut.get("left").get());
    }

    @Test
    @DisplayName("Parsing simple parameters, string and integer")
    void testSimpleUnquotedParameters() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("left")).parse(Input.makeInput("left=aligned"));
        Assertions.assertEquals("aligned", sut.get("left").get());
    }

    @Test
    @DisplayName("Parsing simple parameters just present without value")
    void testSimpleParameterJustPresent() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("left", "right")).parse(Input.makeInput("left"));
        Assertions.assertTrue(sut.is("left"));
        Assertions.assertFalse(sut.is("right"));
    }

    @Test
    @DisplayName("Parsing parameters and one comes from user defined macro")
    void testSimpleParametersOneFromUDMacro() throws BadSyntax {
        final var processor = new Processor("{", "}");
        processor.process(Input.makeInput("{@define margin=2}"));
        final var sut = Params.using(processor).keys(Set.of("margin", "top", "left")).parse(Input.makeInput("top=3 left=\"aligned\""));
        Assertions.assertEquals(2, sut.getInt("margin").getAsInt());
        Assertions.assertEquals(3, sut.getInt("top").getAsInt());
        Assertions.assertEquals("aligned", sut.get("left").get());
    }

    @Test
    @DisplayName("Parsing parameters and one comes from user defined macro")
    void testSimpleParametersOneFromUDMacroOverride() throws BadSyntax {
        final var processor = new Processor("{", "}");
        processor.process(Input.makeInput("{@define margin=3}"));
        final var sut = Params.using(processor).keys(Set.of("margin")).parse(Input.makeInput("margin=2"));
        Assertions.assertEquals(2, sut.getInt("margin").getAsInt());
    }

    @Test
    @DisplayName("It is not a problem when a parameter is missing")
    void testMissingParameter() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("margin", "missing")).parse(Input.makeInput("margin=2"));
        Assertions.assertEquals(2, sut.getInt("margin").getAsInt());
    }

    @Test
    @DisplayName("Line can be continued with trailing \\")
    void testContinuationLine() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("margin", "top", "left")).parse(Input.makeInput("margin=2 top=3 \\\n      left=\"aligned\""));
        Assertions.assertEquals(2, sut.getInt("margin").getAsInt());
        Assertions.assertEquals(3, sut.getInt("top").getAsInt());
        Assertions.assertEquals("aligned", sut.get("left").get());
    }

    @Test
    @DisplayName("A multi-line can present on a single line")
    void testMultiLineString() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("left")).parse(Input.makeInput("left=\"\"\"aligned\"\"\""));
        Assertions.assertEquals("aligned", sut.get("left").get());
    }

    @Test
    @DisplayName("A multi-line can present on a multi line")
    void testMultiLineStringML() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("left")).parse(Input.makeInput("left=\"\"\"alig\nned\"\"\""));
        Assertions.assertEquals("alig\nned", sut.get("left").get());
    }

    @Test
    @DisplayName("single-valued parameter returns in a list")
    void testMultiValuedParameterSingleValue() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("left")).parse(Input.makeInput("left=\"aligned\""));
        Assertions.assertEquals("aligned", sut.getList("left").get(0));
    }

    @Test
    @DisplayName("A multi-valued parameter returns in a list")
    void testMultiValuedParameterMultiValue() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("left")).parse(Input.makeInput("left=\"aligned\"left=\"alignad\""));
        Assertions.assertEquals("aligned", sut.getList("left").get(0));
        Assertions.assertEquals("alignad", sut.getList("left").get(1));
    }

    @Test
    @DisplayName("Non-present parameter returns optional empty")
    void testNonPresentEmpty() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("left")).parse(Input.makeInput(""));
        Assertions.assertTrue(sut.get("left").isEmpty());
    }

    @Test
    @DisplayName("Non-present parameter returns empty List")
    void testNonPresentEmptyList() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("left")).parse(Input.makeInput(""));
        Assertions.assertTrue(sut.getList("left").isEmpty());
    }

    @Test
    @DisplayName("UD macro defined value is also returned in a list")
    void testListParametersOneFromUDMacro() throws BadSyntax {
        final var processor = new Processor("{", "}");
        processor.process(Input.makeInput("{@define margin=2}"));
        final var sut = Params.using(processor).keys(Set.of("margin")).parse(Input.makeInput(""));
        Assertions.assertEquals("2", sut.getList("margin").get(0));
    }

    /* -------------------------------------------------------------------------------------------------------------- */

    @Test
    @DisplayName("Throws up for forbidden parameter while parsing")
    void testUnusedParameter() {
        final var processor = new Processor("{", "}");
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).keys(Set.of("")).parse(Input.makeInput("margin")));
    }

    @Test
    @DisplayName("Throws up for forbidden parameter when getting")
    void testUnusedParameterGet() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("")).parse(Input.makeInput(""));
        Assertions.assertThrows(BadSyntax.class, () -> sut.get("wups"));
    }

    @Test
    @DisplayName("Throws up for unterminated string")
    void testUnterminatedString() {
        final var processor = new Processor("{", "}");
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).keys(Set.of("margin")).parse(Input.makeInput("margin=\"")));
    }

    @Test
    @DisplayName("Throws up for unterminated multi-line string")
    void testUnterminatedMLString() {
        final var processor = new Processor("{", "}");
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).keys(Set.of("margin")).parse(Input.makeInput("margin=\"\"\"")));
    }

    @Test
    @DisplayName("Throws up for single line string in multi-lines")
    void testUnterminatedLineString() {
        final var processor = new Processor("{", "}");
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).keys(Set.of("margin")).parse(Input.makeInput("margin=\"\n\"")));
    }

    @Test
    @DisplayName("Throws up on unquoted empty string")
    void testUnquotedEmptyString() throws BadSyntax {
        final var processor = new Processor("{", "}");
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).keys(Set.of("margin", "left")).parse(Input.makeInput("margin= left=5")));
    }

    @Test
    @DisplayName("Handles unquoted empty string at the last position")
    void testUnquotedEmptyStringLast() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var sut = Params.using(processor).keys(Set.of("margin", "left")).parse(Input.makeInput("margin=5 left="));
        Assertions.assertEquals("", sut.get("left").get());
    }

}

