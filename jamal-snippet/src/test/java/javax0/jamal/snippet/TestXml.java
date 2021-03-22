package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestXml {

    @Test
    @DisplayName("Read the pom.xml and fetch the artifactId from it")
    void canReadThePomXml() throws Exception {
        TestThat.theInput("{@snip:xml pom=pom.xml}{pom /project/artifactId/text()}").results("jamal-snippet");
    }

    @Test
    @DisplayName("Read the pom.xml and fetch the artifactId from it")
    void canReadThePomXmlToGlobal() throws Exception {
        TestThat.theInput("{@snip:xml top:pom=pom.xml}{top:pom /project/artifactId/text()}").results("jamal-snippet");
    }

    @Test
    @DisplayName("Throws exception when the XPath parameter is wrong")
    void badXPath() throws Exception {
        TestThat.theInput("{@snip:xml pom=pom.xml}{pom assd uusa d saud}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Throws exception when the file is not there")
    void badFile() throws Exception {
        TestThat.theInput("{@snip:xml pom=pirim_param-pom.xml}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Throws exception when the file is not specified")
    void noFile() throws Exception {
        TestThat.theInput("{@snip:xml pom}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Wrong xPath throws BadSyntax")
    void cantUsingMacroAsXML() throws Exception {
        TestThat.theInput("{@define pom=pom.xml}{pom...uiaer..n88/ufd()}").throwsBadSyntax();
    }
}
