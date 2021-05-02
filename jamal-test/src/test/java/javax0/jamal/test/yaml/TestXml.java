package javax0.jamal.test.yaml;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestXml {

    @Test
    void testXml() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: alma\n" +
            "lists:\n" +
            "- a\n" +
            "- b\n" +
            "- c\n" +
            "- d\n" +
            "kuka:\n" +
            "  beno:\n" +
            "    \"665-453\"\n" +
            "}{#xmlFormat {@yaml:xml a}}"
        ).results("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<xml>\n" +
            "    <a>a</a>\n" +
            "    <lists>\n" +
            "        <list>a</list>\n" +
            "        <list>b</list>\n" +
            "        <list>c</list>\n" +
            "        <list>d</list>\n" +
            "    </lists>\n" +
            "    <kuka>\n" +
            "        <beno>beno</beno>\n" +
            "    </kuka>\n" +
            "</xml>"
        );
    }

    @Test
    void testXmlFailsOnRecursiveDataStructure() throws Exception {
        TestThat.theInput("" +
            "{#yaml:define a=\n" +
            "a: alma\n" +
            "lists:\n" +
            "- a\n" +
            "- {@yaml:ref a}\n" +
            "}{@yaml:xml a}"
        ).throwsBadSyntax("Jamal source seems to have infinite recursion");
    }
}
