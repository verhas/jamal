package javax0.jamal.ruby;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestMacros {

    @Test
    @DisplayName("Test a simple ruby eval")
    void testSimpleEval() throws Exception {
        TestThat.theInput("{@ruby:eval 6+3}").results("9");
    }

    @Test
    @DisplayName("Test the multiple evals do keep variables")
    void testSimpleMultipleEval() throws Exception {
        TestThat.theInput(
            "{@ruby:eval $myVar = 3\n" +
                "2}" +
                "{@ruby:eval $myVar}"
        ).results("23");
    }

    @Test
    @DisplayName("Test ruby can be started via eval as JSR")
    void testEvalJSRIntegration() throws Exception {
        TestThat.theInput(
            "{@eval/ruby $z = \"\"\n" +
                "for it in 0..9 do\n" +
                "  $z+=it.to_s\n" +
                "end\n" +
                "$z}"
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
                "{%@ruby:property z=(int)55%}" +
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
    @DisplayName("Test complex Ruby shell example with methods defined in the shell")
    void testCatchRubyErrorWithTryMacro() throws Exception {
        TestThat.theInput(
            "{%@try! {%@ruby:eval z%}%}"
        ).usingTheSeparators("{%", "%}").results("Error evaluating ruby script using eval");
    }

    @Test
    @DisplayName("Test that ruby closer works returning null")
    void testRubyCloserNull() throws Exception {
        TestThat.theInput(
            "This is a simple text" +
                "{%@ruby:closer\n" +
                "i=0\n" +
                "while  i < result.length do\n" +
                "  result.insert(i,' ')\n" +
                "  i += 2\n" +
                "end\n" +
                "nil\n" +
                "%}"
        ).usingTheSeparators("{%", "%}").results(" T h i s   i s   a   s i m p l e   t e x t");
    }

    @Test
    @DisplayName("Test that ruby closer works returning the string builder")
    void testRubyCloserResult() throws Exception {
        TestThat.theInput(
            "This is a simple text" +
                "{%@ruby:closer\n" +
                "i=0\n" +
                "while  i < result.length do\n" +
                "  result.insert(i,' ')\n" +
                "  i += 2\n" +
                "end\n" +
                "result\n" +
                "%}"
        ).usingTheSeparators("{%", "%}").results(" T h i s   i s   a   s i m p l e   t e x t");
    }

    @Test
    @DisplayName("Test that ruby closer works returning the string builder")
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
                "'*'+result.toString()+'*'" +
                "%}" +
                "{%@ruby:closer " +
                "'+'+result.toString()+'+'" +
                "%}"
        ).usingTheSeparators("{%", "%}").results("+*AAA*+");
    }
}
