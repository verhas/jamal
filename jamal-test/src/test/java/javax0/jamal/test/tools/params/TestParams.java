package javax0.jamal.test.tools.params;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.Params;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static javax0.jamal.test.tools.params.ParamsTestSupport.keys;

public class TestParams {

    /**
     * snippet doc_testSimpleParameters
     * The integer parameters are not enclosed between `"` characters, although it is perfectly okay to do so.
     * On the other hand the value `"aligned"` is specified between quotes.
     * This value is also eligible to be specified without `"` as it contains neither space, not special escape character or the parsing closing character, which was `\n` in this case.
     * end snippet
     * @throws Exception
     */
    @Test
    @DisplayName("Parsing simple parameters, string and integer")
    void testSimpleParameters() throws Exception {
        keys("margin:I,top:I,left:S").input("margin=2 top=3 left=\"aligned\"").results(
            // snippet testSimpleParameters
            "margin:I,top:I,left:S\n" +
                "input:\n" +
                "margin=2 top=3 left=\"aligned\"\n" +
                "result:\n" +
                "margin=2\n" +
                "top=3\n" +
                "left=\"aligned\""
            // end snippet
        );
    }

    /**
     * snippet doc_testSimpleParameterJustPresent
     * Boolean `true` parameters can be represented by the appearance of the parameter on the line.
     * In this example the parameter`left` simple appears on the input without any value.
     * The parameter `right` does not and it is also not set to `true` as an option, so the value if false.
     * end snippet
     * @throws Exception
     */
    @Test
    @DisplayName("Parsing simple parameters just present without value")
    void testSimpleParameterJustPresent() throws Exception {
        keys("left:B,right:B").input("left").results(
            // snippet testSimpleParameterJustPresent
            "left:B,right:B\n" +
                "input:\n" +
                "left\n" +
                "result:\n" +
                "left=(boolean)true\n" +
                "right=(boolean)false"
            // end snippet
        );
    }

    /**
     * snippet doc_testSimpleParametersOneFromUDMacro
     * In this example two values are present as parameters, but the parameter `margin` is present by a user defined macro.
     * end snippet
     * @throws Exception
     */
    @Test
    @DisplayName("Parsing parameters and one comes from user defined macro")
    void testSimpleParametersOneFromUDMacro() throws Exception {
        keys("margin:I,top:I,left:S").process("{@define margin=2}").input("top=3 left=\"aligned\"").results(
            // snippet testSimpleParametersOneFromUDMacro
            "margin:I,top:I,left:S\n" +
                "input:\n" +
                "{@define margin=2}\n" +
                "top=3 left=\"aligned\"\n" +
                "result:\n" +
                "margin=2\n" +
                "top=3\n" +
                "left=\"aligned\""
            // end snippet
        );
    }

    @Test
    @DisplayName("Parsing parameters and one comes from user defined macro")
    void testSimpleParametersOneFromUDMacroOverride() throws Exception {
        keys("margin:I").process("{@define margin=3}").input("margin=2").results(
            "margin:I\n" +
                "input:\n" +
                "{@define margin=3}\n" +
                "margin=2\n" +
                "result:\n" +
                "margin=2"
        );
    }

    @Test
    @DisplayName("It is a problem when a parameter is missing")
    void testMissingParameter() {
        keys("margin:I,missing:S").input("margin=2").throwsUp();
    }

    @Test
    @DisplayName("Line can be continued with trailing \\")
    void testContinuationLine() throws Exception {
        keys("margin:I,top:I,left:S").input("margin=2 top=3 \\\n      left=\"aligned\"").results(
            "margin:I,top:I,left:S\n" +
                "input:\n" +
                "margin=2 top=3 \\\n" +
                "      left=\"aligned\"\n" +
                "result:\n" +
                "margin=2\n" +
                "top=3\n" +
                "left=\"aligned\""
        );
    }

    @Test
    @DisplayName("A multi-line can present on a single line")
    void testMultiLineString() throws Exception {
        keys("left:S").input("left=\"\"\"aligned\"\"\"").results(
            "left:S\n" +
                "input:\n" +
                "left=\"\"\"aligned\"\"\"\n" +
                "result:\n" +
                "left=\"aligned\""
        );
    }

    @Test
    @DisplayName("A multi-line can present on a multi line")
    void testMultiLineStringML() throws Exception {
        keys("left:S").input("left=\"\"\"alig\nned\"\"\"").results(
            "left:S\n" +
                "input:\n" +
                "left=\"\"\"alig\n" +
                "ned\"\"\"\n" +
                "result:\n" +
                "left=\"alig\n" +
                "ned\""
        );
    }

    @Test
    @DisplayName("single-valued parameter returns in a list")
    void testMultiValuedParameterSingleValue() throws Exception {
        keys("left:L").input("left=\"aligned\"").results(
            "left:L\n" +
                "input:\n" +
                "left=\"aligned\"\n" +
                "result:\n" +
                "left=[aligned]"
        );
    }

    @Test
    @DisplayName("A multi-valued parameter returns in a list")
    void testMultiValuedParameterMultiValue() throws Exception {
        keys("left:L").input("left=\"aligned\"left=\"alignad\"").results(
            "left:L\n" +
                "input:\n" +
                "left=\"aligned\"left=\"alignad\"\n" +
                "result:\n" +
                "left=[aligned,alignad]"
        );
    }

    @Test
    @DisplayName("Non-present parameter returns empty List")
    void testNonPresentEmptyList() throws Exception {
        keys("left:L").input("").results(
            "left:L\n" +
                "input:\n" +
                "\n" +
                "result:\n" +
                "left=[]"
        );
    }

    @Test
    @DisplayName("UD macro defined value is also returned in a list")
    void testListParametersOneFromUDMacro() throws Exception {
        keys("margin:L").process("{@define margin=2}").input("").results(
            "margin:L\n" +
                "input:\n" +
                "{@define margin=2}\n" +
                "\n" +
                "result:\n" +
                "margin=[2]"
        );
    }

    @Test
    @DisplayName("UD macro value is not considered for boolean value")
    void testNoUDMacroForBoolean() throws Exception {
        keys("margin:B").process("{@define margin=true}").input("").results(
            "margin:B\n" +
                "input:\n" +
                "{@define margin=true}\n" +
                "\n" +
                "result:\n" +
                "margin=(boolean)false"
        );
    }

    @Test
    @DisplayName("Parsing simple parameters between ( and )")
    void testSimpleParametersBetweenParens() throws Exception {
        keys("margin:I,top:I,left:S").startWith('(').endWith(')').input(" (margin=2 top=3 left=\"aligned\")").results(
            "margin:I,top:I,left:S\n" +
                "input:\n" +
                " (margin=2 top=3 left=\"aligned\")\n" +
                "result:\n" +
                "margin=2\n" +
                "top=3\n" +
                "left=\"aligned\""
        );
    }

    @Test
    @DisplayName("Parsing simple parameters between ( and ) on multi line")
    void testSimpleParametersBetweenParensML() throws Exception {
        keys("margin:I,top:I,left:S").startWith('(').endWith(')').input(" (margin=2 top=3 \nleft=\"aligned\")").results(
            "margin:I,top:I,left:S\n" +
                "input:\n" +
                " (margin=2 top=3 \n" +
                "left=\"aligned\")\n" +
                "result:\n" +
                "margin=2\n" +
                "top=3\n" +
                "left=\"aligned\""
        );
    }

    @Test
    @DisplayName("Parsing simple parameters with alternative names")
    void testSimpleParametersAlternatives() throws Exception {
        keys("margin|margarethe:I,left:S,top:I").input(" margarethe=2 top=3 left=\"aligned\"").results(
            "margin|margarethe:I,left:S,top:I\n" +
                "input:\n" +
                " margarethe=2 top=3 left=\"aligned\"\n" +
                "result:\n" +
                "margin=2\n" +
                "left=\"aligned\"\n" +
                "top=3"
        );
    }

    @Test
    @DisplayName("Parsing simple parameters with alternative names, first is taken from user defined macro")
    void testSimpleParametersAlternativesUDYes() throws Exception {
        keys("margin|margarethe:I,left:S,top:I").process("{@define margin=2}")
            .input("margarethe=7 top=3 left=\"aligned\"").results(
            "margin|margarethe:I,left:S,top:I\n" +
                "input:\n" +
                "{@define margin=2}\n" +
                "margarethe=7 top=3 left=\"aligned\"\n" +
                "result:\n" +
                "margin=7\n" +
                "left=\"aligned\"\n" +
                "top=3"
        );
    }

    @Test
    @DisplayName("Parsing simple parameters with alternative names, alternative name is NOT taken from user defined macro")
    void testSimpleParametersAlternativesUDNo() throws Exception {
        keys("margin|margarethe:I:5,left:S,top:I").process("{@define margarethe=2}")
            .input("top=3 left=\"aligned\"").results(
            "margin|margarethe:I:5,left:S,top:I\n" +
                "input:\n" +
                "{@define margarethe=2}\n" +
                "top=3 left=\"aligned\"\n" +
                "result:\n" +
                "margin=5\n" +
                "left=\"aligned\"\n" +
                "top=3"
        );
    }

    /**
     * snippet doc_testBooleanParameters
     *  The parameter `trueOption` is set globally calling the macro `options`.
     * The `explicitFalseOption` is set to false on the same line.
     * This is an example about how to set and reset options, even more than one at the same time.
     *
     * * The parameter `implicitFalseOption` is not set anywhere.
     * It is required by the macro, it is notdefined as an option and also not as a parameter.
     * This parameter will be `false` by default.
     *
     * * The parameter `falseAsNo` is set to `no` as a parameter.
     * Similarly `falseAsFalse` is set to `false`, `falseAs0` is set to `0`.
     *
     * * As the false parameters are listed with all the values the `true` values are also listed with some of the possible assignment values that result a `true` value.
     * `trueAsTrue` is set to `true`. The parameter `trueAsYes` is set to `yes`, `trueAs1` is set to `1`.
     * Finally `trueAsAnything` is set to an arbitrary string that will be converted to a true value.
     *
     * * The parameter `trueStandalone` demonstrate the use of a boolean parameter when the name is simply listed as a parameter without any value.
     * In this case the presence of the parameter signals the true value it presents.
     *
     * Using some arbitrary value to signal a boolean value is usually not the best choice.
     * Other than choosing presenting the value in the form of a standalone parameter, or with value `yes`, `true`, `no`, `0`, `false` is a matter of taste.
     * Use the one that you feel makes your code the most readable.
     * Jamal source can get very easily really messy and complex.
     * Strive to make it as simple as possible.
     * end snippet
     * @throws Exception
     */
    @Test
    @DisplayName("Boolean parameters defined in different ways")
    void testBooleanParameters() throws Exception {
        keys("trueOption:B," +
            "explicitFalseOption:B," +
            "implicitFalseOption:B," +
            "falseAsNo:B," +
            "falseAsFalse:B," +
            "falseAs0:B," +
            "trueAsTrue:B," +
            "trueAsYes:B," +
            "trueAs1:B," +
            "trueAsAnything:B," +
            "trueStandalone:B").process("{@options trueOption|~explicitFalseOption}").input(
            "falseAsNo=no " +
                "falseAsFalse=false " +
                "falseAs0=0 " +
                "trueAsTrue=true \\\n" +
                "trueAsYes=yes " +
                "trueAs1=1 " +
                "trueAsAnything=\"really anything goes\" " +
                "trueStandalone"
        ).results(
            // snippet testBooleanParameters
            "trueOption:B,explicitFalseOption:B,implicitFalseOption:B,falseAsNo:B,falseAsFalse:B," +
                "falseAs0:B,trueAsTrue:B,trueAsYes:B,trueAs1:B,trueAsAnything:B,trueStandalone:B\n" +
                "input:\n" +
                "{@options trueOption|~explicitFalseOption}\n" +
                "falseAsNo=no falseAsFalse=false falseAs0=0 trueAsTrue=true \\\n" +
                "trueAsYes=yes trueAs1=1 trueAsAnything=\"really anything goes\" trueStandalone\n" +
                "result:\n" +
                "trueOption=(boolean)true\n" +
                "explicitFalseOption=(boolean)false\n" +
                "implicitFalseOption=(boolean)false\n" +
                "falseAsNo=(boolean)false\n" +
                "falseAsFalse=(boolean)false\n" +
                "falseAs0=(boolean)false\n" +
                "trueAsTrue=(boolean)true\n" +
                "trueAsYes=(boolean)true\n" +
                "trueAs1=(boolean)true\n" +
                "trueAsAnything=(boolean)true\n" +
                "trueStandalone=(boolean)true"
            // end snippet
        );
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

