package javax0.jamal.test.tools.params;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;
import javax0.jamal.testsupport.TestThat;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.Params;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static javax0.jamal.test.tools.params.ParamsTestSupport.keys;

public class TestParams {

    /* NOTES ON DOCUMENTATION/s n i p p e t CONTAINED IN THIS UNIT TEST FILE
     *
     * Each unit test that plays documentation purpose should define three s n i p p e t s:
     *
     * - head_{testName} that contains the title of the section and also optionally a few words about the example
     * - {testName} that contains the expected string. No multi-line strings. Each line starts with " character
     *   and ends with '\n" +' characters, except the last line. These will be removed by the macros and the text will
     *   also be trimmed.
     * - doc_{testName} is the explanation that follows the result.
     *
     * {testName} is the name of the unit test method. It is only convention, it could be anything unique, but there is
     * no reason to make it more complex.
     *
     * DO NOT USE '<p>' in the comments. However, if you do, they will be removed by the jamal processing.
     *
     * You can use Jamal macros in the snippet. They will be evaluated as 'head_' , result, 'doc_', which is not the
     * order as they usually appear in the file. On the other hand the s n i p p e t triplets are used in the order as they
     * appear in the file.
     *
     * HINTS:
     *
     * Leave an empty line between the 's n i p p e t' and the content and also between
     * `e n d s n i p p e t` and content.
     *
     */


    /**
     * snippet head_testSimpleParameters
     * <p>
     * {%section Simple Parameters%}
     * <p>
     * This example shows the simple use of two integer, and a string parameter use.
     * <p>
     * end snippet
     * <p>
     * snippet doc_testSimpleParameters
     * <p>
     * The integer parameters are not enclosed between `"` characters, although it is perfectly okay to do so. On the
     * other hand the value `"aligned"` is specified between quotes. This value is also eligible to be specified without
     * `"` as it contains neither space, not special escape character or the parsing closing character, which was `\n`
     * in this case.
     * <p>
     * end snippet
     *
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
     * snippet head_testSimpleParameterJustPresent
     * <p>
     * {%section Simple Boolean Example%}
     * <p>
     * Boolean parameters can be specified by the sheer presence. When a boolean parameter is not present and not
     * defined as an option, then the value is `false`.
     * <p>
     * end snippet
     * <p>
     * snippet doc_testSimpleParameterJustPresent
     * <p>
     * Boolean `true` parameters can be represented by the appearance of the parameter on the line. In this example the
     * parameter`left` simple appears on the input without any value. The parameter `right` does not and it is also not
     * set to `true` as an option, so the value if false.
     * <p>
     * end snippet
     *
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
     * snippet head_testSimpleParametersOneFromUDMacro
     * <p>
     * {%section Parameter Defined as User Defined Macro%}
     * <p>
     * end snippet
     * <p>
     * snippet doc_testSimpleParametersOneFromUDMacro
     * <p>
     * In this example two values are present as parameters, but the parameter
     * `margin` is present by a user defined macro.
     * <p>
     * end snippet
     *
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

    /**
     * snippet head_testSimpleParametersOneFromUDMacroOverride
     * <p>
     * {%section Value defined in User-defined Macro is Overridden by parameter%}
     * <p>
     * This example shows that a parameter defined in a user-defined macro is overridden by the definition of the
     * parameter on the input.
     * <p>
     * end snippet
     * <p>
     * snippet doc_testSimpleParametersOneFromUDMacroOverride
     * <p>
     * The parameter `margin` is defined as a user defined parameter, but the value `3` is ignored because it is also
     * defined on the input to be `2` and this is stronger.
     * <p>
     * end snippet
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Parsing parameters and one comes from user defined macro")
    void testSimpleParametersOneFromUDMacroOverride() throws Exception {
        keys("margin:I").process("{@define margin=3}").input("margin=2").results(
                // snippet testSimpleParametersOneFromUDMacroOverride
                "margin:I\n" +
                        "input:\n" +
                        "{@define margin=3}\n" +
                        "margin=2\n" +
                        "result:\n" +
                        "margin=2"
                // end snippet
        );
    }

    /**
     * snippet head_testMissingParameter
     * {%section Missing Parameter%}
     * <p>
     * When a parameter is used by a macro and there is no default value
     * for the parameter then not defining the parameter will be an error.
     * <p>
     * end snippet
     * <p>
     * snippet doc_testMissingParameter
     * <p>
     * The sample macro configuration requires two parameters: `margin` and `missing`.
     * None of them has default value and they are also no boolean or list values.
     * Margin is defined in the input but the parameter `missing`, aptly named, is indeed missing.
     * This makes the parameter parsing to throw an exception.
     * <p>
     * end snippet
     */
    @Test
    @DisplayName("It is a problem when a parameter is missing")
    void testMissingParameter() {
        keys("margin:I,missing:S").input("margin=2").throwsUp(
                // snippet testMissingParameter
                "margin:I,missing:S\n" +
                        "input:\n" +
                        "margin=2\n" +
                        "result:\n" +
                        "javax0.jamal.api.BadSyntax: The key 'missing' for the macro 'test environment' is mandatory"
                // end snippet
        );
    }

    /**
     * snippet head_testContinuationLine
     * <p>
     * {%section Continuation line%}
     * <p>
     * This example shows that the first line can be extended using continuation lines, which are escaped using `\`
     * character at the end of the line.
     * <p>
     * end snippet
     * <p>
     * snippet doc_testContinuationLine
     * The parameters `margin` and `top` are defined on the first line.
     * The parameter `left` would have been too long.
     * It got into the next line.
     * To do that the last character on the previous line is a `\` character.
     * <p>
     * end snippet
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Line can be continued with trailing \\")
    void testContinuationLine() throws Exception {
        keys("margin:I,top:I,left:S").input("margin=2 top=3 \\\n      left=\"aligned\"").results(
                // snippet testContinuationLine
                "margin:I,top:I,left:S\n" +
                        "input:\n" +
                        "margin=2 top=3 \\\n" +
                        "      left=\"aligned\"\n" +
                        "result:\n" +
                        "margin=2\n" +
                        "top=3\n" +
                        "left=\"aligned\""
                // end snippet
        );
    }

    /**
     * snippet head_testMultiLineString
     * {%section Multi-line String parameter, one line%}
     * <p>
     * This example shows how you can use multi-line strings as parameters.
     * Multi-line strings start and end with the `"""` characters and can span multiple lines.
     * In this example the sample multi-line string does not span multiple line showing that this is not a must.
     * The use also demonstrates that single `"` characters do not need to be escaped, but they may be escaped.
     * <p>
     * end snippet
     * <p>
     * snippet doc_testMultiLineString
     * <p>
     * The value of the parameter`left` is specified as a multi-line string, and it contains two `"` characters, one escaped, the other without escaping.
     * <p>
     * end snippet
     *
     * @throws Exception
     */
    @Test
    @DisplayName("A multi-line can present on a single line")
    void testMultiLineString() throws Exception {
        keys("left:S").input("left=\"\"\"ali\"gn\\\"ed\"\"\"").results(
                // snippet testMultiLineString
                "left:S\n" +
                        "input:\n" +
                        "left=\"\"\"ali\"gn\\\"ed\"\"\"\n" +
                        "result:\n" +
                        "left=\"ali\\\"gn\\\"ed\""
                //end snippet
        );
    }

    /**
     * snippet head_testMultiLineStringML
     * {%section Multi-line String parameter, two lines%}
     * <p>
     * This example shows how you can use multi-line strings as parameters.
     * Multi-line strings start and end with the `"""` characters and can span multiple lines.
     * In this example the sample multi-line string spans two lines.
     * <p>
     * end snippet
     * <p>
     * snippet doc_testMultiLineStringML
     * <p>
     * This time the parameter `aligned` contains a new line in the string.
     * <p>
     * end snippet
     *
     * @throws Exception
     */
    @Test
    @DisplayName("A multi-line can present on a multi line")
    void testMultiLineStringML() throws Exception {
        keys("left:S").input("left=\"\"\"alig\nned\"\"\"").results(
                // snippet testMultiLineStringML
                "left:S\n" +
                        "input:\n" +
                        "left=\"\"\"alig\n" +
                        "ned\"\"\"\n" +
                        "result:\n" +
                        "left=\"alig\\nned\""
                // end snippet
        );
    }

    /**
     * snippet head_testMultiValuedParameterSingleValue
     * {%section Multi-valued parameter can have single value%}
     * <p>
     * Multi-valued parameters can apper more than once as parameter.
     * But it is not a must.
     * They may be missing, or specified only one time.
     * This example shows that a multi-valued parameter can appear one time.
     * <p>
     * end snippet
     * <p>
     * snippet doc_testMultiValuedParameterSingleValue
     * <p>
     * The parameter `left` is a `L` list as it is declared by the testing macro.
     * Even though it is a list it appears only once as a parameter.
     * The result for the macro is that this parameter will be a list that has a single element.
     * <p>
     * end snippet
     *
     * @throws Exception
     */
    @Test
    @DisplayName("single-valued parameter returns in a list")
    void testMultiValuedParameterSingleValue() throws Exception {
        keys("left:L").input("left=\"aligned\"").results(
                // snippet testMultiValuedParameterSingleValue
                "left:L\n" +
                        "input:\n" +
                        "left=\"aligned\"\n" +
                        "result:\n" +
                        "left=[aligned]"
                // end snippet
        );
    }

    /**
     * snippet head_testMultiValuedParameterMultiValue
     * <p>
     * {%section Multi-valued Parameter with Multiple Values%}
     * <p>
     * This example shows how to specify multiple values for a parameter that is declared to have multiple values.
     * <p>
     * end snippet
     * <p>
     * snippet doc_testMultiValuedParameterMultiValue
     * <p>
     * end snippet
     *
     * @throws Exception
     */
    @Test
    @DisplayName("A multi-valued parameter returns in a list")
    void testMultiValuedParameterMultiValue() throws Exception {
        keys("left:L").input("left=\"aligned\"left=\"alignad\"").results(
                // snippet testMultiValuedParameterMultiValue
                "left:L\n" +
                        "input:\n" +
                        "left=\"aligned\"left=\"alignad\"\n" +
                        "result:\n" +
                        "left=[aligned,alignad]"
                //end snippet
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
        keys("margin:I,top:I,left:S").between("()").input(" (margin=2 top=3 left=\"aligned\")").results(
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
        keys("margin:I,top:I,left:S").between("()").input(" (margin=2 top=3 \nleft=\"aligned\")").results(
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
    @DisplayName("When multiple holders use the same key it throws Illegal Argument Exception")
    void testMultipleKeyUseError() throws Exception {
        final var file = Params.<String>holder("", "file").asString();
        final var name = Params.<String>holder("", "name").asString();
        Assertions.assertThrows(IllegalArgumentException.class, () -> Params.using(null)
                .from(() -> this.getClass().getSimpleName()).tillEnd().keys(file, name));
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
     * snippet head_testBooleanParameters
     * <p>
     * {%section Boolean Parameters%}
     * <p>
     * This example shows an extensive list of all the possibilities how a boolean parameter can be defined.
     * <p>
     * end snippet
     * <p>
     * snippet doc_testBooleanParameters
     * <p>
     * The parameter `trueOption` is set globally calling the macro `options`. The `explicitFalseOption` is set to false
     * on the same line. This is an example about how to set and reset options, even more than one at the same time.
     * <p>
     * * The parameter `implicitFalseOption` is not set anywhere. It is required by the macro, it is notdefined as an
     * option and also not as a parameter. This parameter will be `false` by default.
     * <p>
     * * The parameter `falseAsNo` is set to `no` as a parameter. Similarly `falseAsFalse` is set to `false`, `falseAs0`
     * is set to `0`.
     * <p>
     * * As the false parameters are listed with all the values the `true` values are also listed with some of the
     * possible assignment values that result a `true` value. `trueAsTrue` is set to `true`. The parameter `trueAsYes`
     * is set to `yes`, `trueAs1` is set to `1`. Finally `trueAsAnything` is set to an arbitrary string that will be
     * converted to a true value.
     * <p>
     * * The parameter `trueStandalone` demonstrate the use of a boolean parameter when the name is simply listed as a
     * parameter without any value. In this case the presence of the parameter signals the true value it presents.
     * <p>
     * Using some arbitrary value to signal a boolean value is usually not the best choice. Other than choosing
     * presenting the value in the form of a standalone parameter, or with value `yes`, `true`, `no`, `0`, `false` is a
     * matter of taste. Use the one that you feel makes your code the most readable. Jamal source can get very easily
     * really messy and complex. Strive to make it as simple as possible.
     * <p>
     * end snippet
     *
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
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).from(() -> this.getClass().getSimpleName()).keys().parse(Input.makeInput("margin")));
    }

    @Test
    @DisplayName("Throws up for unterminated string")
    void testUnterminatedString() {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin");
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).from(() -> this.getClass().getSimpleName()).keys(margin).parse(Input.makeInput("margin=\"")));
    }

    @Test
    @DisplayName("Throws up for unterminated multi-line string")
    void testUnterminatedMLString() {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin").asInt();
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).from(() -> this.getClass().getSimpleName()).keys(margin).parse(Input.makeInput("margin=\"\"\"")));
    }

    @Test
    @DisplayName("Throws up for single line string in multi-lines")
    void testUnterminatedLineString() {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin").asInt();
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).from(() -> this.getClass().getSimpleName()).keys(margin).parse(Input.makeInput("margin=\"\n\"")));
    }

    @Test
    @DisplayName("Throws up on unquoted empty string")
    void testUnquotedEmptyString() {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin").asInt();
        final var left = Params.<String>holder("left");
        Assertions.assertThrows(BadSyntax.class, () -> Params.using(processor).from(() -> this.getClass().getSimpleName()).keys(margin, left).parse(Input.makeInput("margin= left=5")));
    }

    @Test
    @DisplayName("Handles unquoted empty string at the last position")
    void testUnquotedEmptyStringLast() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var margin = Params.<Integer>holder("margin").asInt();
        final var left = Params.<String>holder("left");
        Params.using(processor).from(() -> this.getClass().getSimpleName()).keys(margin, left).parse(Input.makeInput("margin=5 left="));
        Assertions.assertEquals("", left.get());
    }


    @Test
    @DisplayName("Tests that the parameters can be fetched using 'fetchParameters()'")
    void testFetchParameters() throws BadSyntax {
        final var processor = new Processor("{", "}");
        final var parameters = Params.using(processor).fetchParameters(Input.makeInput("margin=5 left="));
        Assertions.assertEquals(2, parameters.size());
        Assertions.assertEquals("5", parameters.get("margin"));
        Assertions.assertEquals("", parameters.get("left"));
        int i = 0;
        for (var parameter : parameters.entrySet()) {
            switch (i) {
                case 0:
                    Assertions.assertEquals("margin", parameter.getKey());
                    Assertions.assertEquals("5", parameter.getValue());
                    break;
                case 1:
                    Assertions.assertEquals("left", parameter.getKey());
                    Assertions.assertEquals("", parameter.getValue());
                    break;
            }
            i++;
        }
    }

    @Test
    @DisplayName("When an undefined key is used calling a macro the error contains a suggestion")
    void testUndefinedKey() throws Exception {
        TestThat.theInput("{@if [enpty] //}").throwsBadSyntax("The key 'enpty' is not used by the macro 'if'\\. Did you mean 'empty'\\?");
    }
}

