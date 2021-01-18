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
    @DisplayName("Wrong xPath throws BadSyntax")
    void cantUsingMacroAsXML() throws Exception {
        TestThat.theInput("{@define pom=pom.xml}{pom...uiaer..n88/ufd()}").throwsBadSyntax();
    }
}
