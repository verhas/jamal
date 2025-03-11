package javax0.jamal.ruby;

import javax0.jamal.testsupport.SentinelSmith;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestRubyMacros {

    @BeforeEach
    void setUp() throws Exception {
        SentinelSmith.forge("ruby");
    }


    @Test
    @DisplayName("Test a simple ruby eval")
    void testSimpleEval() throws Exception {
        TestThat.theInput("{@ruby:eval 6+3}").results("9");
    }

    @Test
    void testDifferentShells() throws Exception {
        TestThat.theInput("{@ruby:eval $z = 13}\n" +
                "{@define rubyShell=myLocalShell}\n" +
                "{@try! {@ruby:eval $z}}").results("13\n" +
                "\n" +
                "null");
    }

    @Test
    @DisplayName("Test the multiple evals do keep global variables")
    void testSimpleMultipleEval() throws Exception {
        TestThat.theInput(
                "{@ruby:eval $myVar = 3\n" +
                        "2}" +
                        "{@ruby:eval $myVar}"
        ).results("23");
    }

    @Test
    @DisplayName("Test the multiple evals does not keep local variables")
    void testErroneousMultipleEval() throws Exception {
        TestThat.theInput(
                "{@ruby:eval myVar = 3\n" +
                        "2}{@ruby:eval myVar}"
        ).throwsBadSyntax(".*NameError.*");
    }

    @Test
    @DisplayName("Test ruby can be started via eval as JSR")
    void testEvalJSRIntegration() throws Exception {
        TestThat.theInput(
                "{@eval/ruby z = \"\"\n" +
                        "(0..9).each { |it|  z+=it.to_s }\n" +
                        "z}"
        ).results("0123456789");
    }

    @Test
    @DisplayName("Test simple Ruby shell example")
    void testSimpleShell() throws Exception {
        TestThat.theInput(
                "{#sep {@escape `|`{% %}`|`}}\\\n" +
                        "{%@import res:ruby.jim%}\\\n" +
                        "{%#ruby:shell {%shell=engine%}\n" +
                        "$z = \"\"\n" +
                        "for it in 0..9 do\n" +
                        "  $z+=it.to_s\n" +
                        "end\n" +
                        "$z\n" +
                        "%}"
        ).results("0123456789");
    }

    @Test
    @DisplayName("Test complex Ruby shell example")
    void testMultipleShell() throws Exception {
        TestThat.theInput(
                "{#sep {@escape `|`{% %}`|`}}{%@ruby:shell script\n" +
                        "$z = \"\";%}" +
                        "{%@ruby:shell script\n" +
                        "for it in 0..9 do\n" +
                        "  $z+=it.to_s\n" +
                        "end\n" +
                        "$z\n" +
                        "%}"
        ).results("0123456789");
    }

    @Test
    @DisplayName("Erroneous ruby property setting")
    void testErroneoutRubyPropertySetting() throws Exception {
        TestThat.theInput("{@ruby:property $z 13}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Erroneous ruby property setting bad conversion")
    void testErroneoutRubyPropertySettingConversionErr() throws Exception {
        TestThat.theInput("{@ruby:property $z=(to_i)jamm}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Erroneous ruby property setting bad rational conversion")
    void testErroneoutRubyPropertySettingRationalConversionErr() throws Exception {
        TestThat.theInput("{@ruby:property $z=(to_r)12}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Erroneous ruby property setting bad complex conversion")
    void testErroneoutRubyPropertySettingComplexConversionErr() throws Exception {
        TestThat.theInput("{@ruby:property $z=(to_c/i)12}").throwsBadSyntax();
        TestThat.theInput("{@ruby:property $z=(to_c)12}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test complex Ruby shell example with setting and getting a property")
    void testErroneousScript() throws Exception {
        TestThat.theInput(
                "{#sep {@escape `|`{% %}`|`}}" +
                        "{%@ruby:property $z=%}" +
                        "{%@ruby:shell engine script\n" +
                        "asdnjasdasdsasad asdsajdsanjd" +
                        "%}" +
                        "{%@ruby:property $z%}"
        ).throwsBadSyntax();
    }

    @Test
    @DisplayName("Test complex Ruby shell example with setting and getting a property")
    void testMultipleShellSetGetProperty() throws Exception {
        TestThat.theInput(
                "{#sep {@escape `|`{% %}`|`}}" +
                        "{%@ruby:property $z=%}" +
                        "{%@ruby:shell engine script\n" +
                        "for it in 0..9 do\n" +
                        "  $z+=it.to_s\n" +
                        "end\n" +
                        "$z\n" +
                        "%}" +
                        "{%@ruby:property $z%}"
        ).results("01234567890123456789");
    }

    @Test
    @DisplayName("Test complex Ruby shell example with setting and getting casted property")
    void testMultipleShellSetGetCastedProperty() throws Exception {
        TestThat.theInput(
                "{#sep {@escape `|`{% %}`|`}}" +
                        "{%@ruby:property z=(to_i)55%}" +
                        "{%@ruby:shell script\n" +
                        "z *= 2\n" +
                        "for it in 0..9 do\n" +
                        "  z+=it\n" +
                        "end\n" +
                        "z\n" +
                        "%}"
        ).results("155");
    }

    @Test
    @DisplayName("Test complex Ruby shell example with methods defined in the shell")
    void testMultipleShellMethods() throws Exception {
        TestThat.theInput(
                "{#sep {@escape `|`{% %}`|`}}" +
                        "{%@ruby:import\n" +
                        "class Main\n" +
                        "  def increment(x)\n" +
                        "     x + 1\n" +
                        "  end\n" +
                        "end\n" +
                        "%}" +
                        "{%@ruby:shell\n" +
                        "Main.new().increment(13)" +
                        "%}"
        ).results("14");
    }

    @Test
    @DisplayName("Test complex Ruby shell example with methods defined in the shell")
    void testMultipleShellMethodsImportResource() throws Exception {
        TestThat.theInput(
                "{%@ruby:import         res:increment.rb%}\\\n" +
                        "{%@ruby:shell\n" +
                        "Main.new().increment(13)" +
                        "%}"
        ).usingTheSeparators("{%", "%}").results("14");
    }

    @Test
    @DisplayName("Throws exception when there is syntax error in the imported ruby file")
    void testSyntaxErrorImport() throws Exception {
        TestThat.theInput(
                "{%@ruby:import         res:syntaxError.rb%}\\\n%}"
        ).usingTheSeparators("{%", "%}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test complex Ruby shell example with methods defined in the shell")
    void testCatchRubyErrorWithTryMacro() throws Exception {
        TestThat.theInput(
                "{%@try! {%@ruby:eval z%}%}"
        ).usingTheSeparators("{%", "%}").results("Error evaluating ruby script using eval");
    }

    @Test
    @DisplayName("Test that ruby conversion to symbol works")
    void testRubyPropertySymbol() throws Exception {
        TestThat.theInput(
                "{%@ruby:property sym=(to_sym)symbole%}" +
                        "{%@ruby:eval sym%}"
        ).usingTheSeparators("{%", "%}").results("symbole");
    }

    @Test
    @DisplayName("Test that ruby conversion to rational works")
    void testRubyPropertyRational() throws Exception {
        TestThat.theInput(
                "{%@ruby:property r1=(to_r)5/3%}" +
                        "{%@ruby:property r2=(to_r)1/3%}" +
                        "{%@ruby:property f1=(to_f)1.66%}" +
                        "{%@ruby:property f2=(to_f)0.33%}" +
                        "{%@ruby:shell\n" +
                        "  (r1+r2).to_s << ' ' << (f1+f2).to_s\n" +
                        "%}"
        ).usingTheSeparators("{%", "%}").results("2/1 1.99");
    }

    @Test
    @DisplayName("Test that ruby conversion to float works")
    void testRubyPropertyFloat() throws Exception {
        TestThat.theInput(
                "{%@ruby:property f=(to_f)5%}" +
                        "{%@ruby:shell\n" +
                        "  (f*f)\n" +
                        "%}"
        ).usingTheSeparators("{%", "%}").results("25.0");
    }

    // snippet sample_snippet
    @Test
    @DisplayName("Test that ruby conversion to fixnum works")
    void testRubyPropertyFixNum() throws Exception {
        TestThat.theInput(
                "{%@define rubyShell=wuff%}" +
                        "{%@ruby:property int=(to_i)5%}" +
                        "{%@ruby:shell\n" +
                        "  (int*int)\n" +
                        "%}"
        ).usingTheSeparators("{%", "%}").results("25");
    }

    // end snippet
    @Test
    @DisplayName("Test shell id can be given using option")
    void testRubyPropertyFixNumWithOption() throws Exception {
        TestThat.theInput(
                "{%@ruby:property (shell=wuff) int=(to_i)5%}" +
                        "{%@ruby:shell (shell=wuff)\n" +
                        "  (int*int)\n" +
                        "%}"
        ).usingTheSeparators("{%", "%}").results("25");
    }

    @Test
    @DisplayName("Test that ruby conversion to complex works")
    void testRubyPropertyComplex() throws Exception {
        TestThat.theInput(
                "{%@ruby:property c=(to_c)1+1i%}{%@ruby:eval c*c%}"
        ).usingTheSeparators("{%", "%}").results("0.0+2.0i");
        TestThat.theInput(
                "{%@ruby:property c=(to_c/i)1+1i%}{%@ruby:eval c*c%}"
        ).usingTheSeparators("{%", "%}").results("0+2i");
    }

    @Test
    @DisplayName("Test that ruby closer works returning null")
    void testRubyCloserNull() throws Exception {
        TestThat.theInput(
                "This is a simple text" +
                        "{%@ruby:closer\n" +
                        "nil\n" +
                        "%}"
        ).usingTheSeparators("{%", "%}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test that ruby closer works returning the same 'result' string")
    void testRubyCloserResult() throws Exception {
        TestThat.theInput(
                "This is a simple text" +
                        "{%@ruby:closer\n" +
                        "i=0\n" +
                        "while  i < $result.length do\n" +
                        "  $result[i] = \" #{$result[i]}\"\n" +
                        "  i += 2\n" +
                        "end\n" +
                        "$result\n" +
                        "%}"
        ).usingTheSeparators("{%", "%}").results(" T h i s   i s   a   s i m p l e   t e x t");
    }

    @Test
    @DisplayName("Test that ruby closer works returning the string")
    void testRubyCloserString() throws Exception {
        TestThat.theInput(
                "This is a simple text" +
                        "{%@ruby:closer\n" +
                        "'I\\'m too old for this shit.'" +
                        "%}"
        ).usingTheSeparators("{%", "%}").results("I'm too old for this shit.");
    }

    @Test
    @DisplayName("Test that ruby closers works if there are multiple, but the order is guaranteed")
    void testRubyClosers() throws Exception {
        TestThat.theInput(
                "AAA" +
                        "{%@ruby:closer " +
                        "'*'+$result+'*'" +
                        "%}" +
                        "{%@ruby:closer " +
                        "'+'+$result+'+'" +
                        "%}"
        ).usingTheSeparators("{%", "%}").results("+*AAA*+");
    }
}
