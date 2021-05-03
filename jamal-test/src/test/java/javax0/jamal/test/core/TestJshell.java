package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestJshell {
    @Test
    void testJShellExecution() throws Exception {
        TestThat.theInput(

            "{@sep (( )) }((@JShell\n" +
                "\n" +
                "void hello(){\n" +
                "    System.out.println(\"hello \" + b);\n" +
                "}\n" +
                "\n" +
                "void hallo(){\n" +
                "    System.out.println(\"hallo \" + a);\n" +
                "}\n" +
                "))\\\n" +
                "((@script hello/JShell(a,b)=hallo();\n" +
                "hello() ))\\\n" +
                "((@script helloBlock/JShell(a,b)=hallo();\n" +
                "hello() ))\\\n" +
                "((hello/Misi/Matyi))\n" +
                "((hello/\"Misi\n" +
                "Matyi\n" +
                "Nagyi\n" +
                "Anya \\ Apa\"/Eszti))\n" +
                "\n" +
                "((helloBlock/Misi/Matyi))")

            .results(

                "hallo Misi\n" +
                    "hello Matyi\n" +
                    "\n" +
                    "hallo \"Misi\n" +
                    "Matyi\n" +
                    "Nagyi\n" +
                    "Anya \\ Apa\"\n" +
                    "hello Eszti\n" +
                    "\n" +
                    "\n" +
                    "hallo Misi\n" +
                    "hello Matyi\n");
    }

    @Test
    void testJShellError() throws Exception {
        TestThat.theInput("{@try!\n" +
            "{@script exit/JShell=System.exit(1);}\\\n" +
            "{exit}\\\n" +
            "{@script hello/JShell=System.out.println(\"hello\");}\\\n" +
            "{hello}}").results("The JShell snippet 'System.exit(1);' closed the JShell interpreter. Will not be recreated.");
    }

    @Test
    void testJShellDocument() throws Exception {
        TestThat.theInput("{@JShell\n" +
            "    void hello(){\n" +
            "        System.out.println(\"Hello, \" + world);\n" +
            "    }\n" +
            "}{@script hello/JShell(world)=hello();}\n" +
            "{hello My Dear}\n" +
            "\n" +
            "{@JShell\n" +
            "    void hallo(){\n" +
            "        System.out.println(\"Hallo, \" + you);\n" +
            "    }\n" +
            "}\n" +
            "{#block\n" +
            "{@script helloBlock/JShell(a,b)=hallo(\n" +
            ");hello()}\n" +
            "{@export helloBlock}}\n" +
            "{hello/My World/You}").results("\n" +
            "Hello, My Dear\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Hello, My World/You\n");
    }
}
