package javax0.jamal.java;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestJavaSourceMacro {

    @Test
    void test() throws Exception {
        TestThat.theInput("{@jbim:macro \n" +
                        "        package javax0.jamal.java.testmacros;\n" +
                        "        \n" +
                        "        import javax0.jamal.api.BadSyntax;\n" +
                        "        import javax0.jamal.api.Input;\n" +
                        "        import javax0.jamal.api.Macro;\n" +
                        "        import javax0.jamal.api.Processor;\n" +
                        "        \n" +
                        "        public class TestMacro implements Macro {\n" +
                        "            @Override\n" +
                        "            public String evaluate(final Input in, final Processor processor) throws BadSyntax {\n" +
                        "                return \"maci\";\n" +
                        "            }\n" +
                        "        \n" +
                        "            @Override\n" +
                        "            public String getId() {\n" +
                        "                return \"maci\";\n" +
                        "            }\n" +
                        "        }\n" +
                        "}" +
                        "{@jbim:moduleinfo}" +
                        "{@jbim:load}" +
                        "{@maci}")
                .results("maci");
    }

}
