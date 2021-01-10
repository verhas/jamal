package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestXml {

    @Test
    void canReadThePomXml() throws Exception {
        TestThat.theInput("{@snip:xml pom=pom.xml}{@snip:xpath pom /project/artifactId/text()}").results("jamal-snippet");
    }
}
