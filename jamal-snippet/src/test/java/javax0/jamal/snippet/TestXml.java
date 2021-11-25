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

    @Test
    @DisplayName("Define an XML from the input")
    void canDefineXml() throws Exception {
        TestThat.theInput("" +
            "{@xml:define myXml=<xml>" +
            "<yml>babu</yml>" +
            "</xml>}{myXml /xml/yml/text()}").results("babu");
    }

    @Test
    @DisplayName("Read POM including")
    void canReadPomIncluding() throws Exception {
        TestThat.theInput("" +
            "{#xml:define pom={@include [verbatim] pom.xml}}" +
            "{pom /project/artifactId/text()}").results("jamal-snippet");
    }

    @Test
    @DisplayName("Insert XML into another XML")
    void insertXml() throws Exception {
        TestThat.theInput("" +
            "{@xml:define pom=<project><dependencies></dependencies></project>}" +
            "{@xml:insert (id=pom path=/project/dependencies) <dependency>hukk</dependency>}" +
            "{pom}"
        ).results("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<project>\n" +
            "    <dependencies>\n" +
            "        <dependency>hukk</dependency>\n" +
            "    </dependencies>\n" +
            "</project>");
    }

    @Test
    @DisplayName("Insert XML into the main XML deferred")
    void insertXmlDeferred() throws Exception {
        TestThat.theInput("" +
            "<project><dependencies></dependencies></project>" +
            "{@xml:insert (path=/project/dependencies) <dependency>hukk</dependency>}"
        ).results("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<project>\n" +
            "    <dependencies>\n" +
            "        <dependency>hukk</dependency>\n" +
            "    </dependencies>\n" +
            "</project>");
    }

    @Test
    @DisplayName("Insert XML into the main XML deferred multiple times")
    void insertMultipleXmlDeferred() throws Exception {
        TestThat.theInput("" +
            "{@xml:insert (path=/project/dependencies tabsize=13546) <dependency>bakk</dependency>}" +
            "<project><dependen" +
            // the last tabsize is relevant
            "{@xml:insert (path=/project/dependencies tabsize=7) <dependency>makk</dependency>}" +
            "cies></dependencies></project>" +
            // the default tabsize does not override the last tabsize
            "{@xml:insert (path=/project/dependencies) <dependency>hukk</dependency>}"
        ).results("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<project>\n" +
            "       <dependencies>\n" +
            "              <dependency>bakk</dependency>\n" +
            "              <dependency>makk</dependency>\n" +
            "              <dependency>hukk</dependency>\n" +
            "       </dependencies>\n" +
            "</project>");
    }

    @Test
    @DisplayName("Insert XML into the main XML deferred multiple times")
    void insertMultipleXmlDeferredToExisting() throws Exception {
        TestThat.theInput("" +
            "{@xml:insert (path=/ ifneeded) <project></project>}" +
            "{@xml:insert (path=/project ifneeded) <dependencies></dependencies>}" +
            "{@xml:insert (path=/project/dependencies tabsize=13546) <dependency>bakk</dependency>}" +
            "<project><dependen" +
            // the last tabsize is relevant
            "{@xml:insert (path=/project/dependencies tabsize=7) <dependency>makk</dependency>}" +
            "cies></dependencies></project>" +
            // the default tabsize does not override the last tabsize
            "{@xml:insert (path=/project/dependencies) <dependency>hukk</dependency>}"
        ).results("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<project>\n" +
            "       <dependencies>\n" +
            "              <dependency>bakk</dependency>\n" +
            "              <dependency>makk</dependency>\n" +
            "              <dependency>hukk</dependency>\n" +
            "       </dependencies>\n" +
            "</project>");
    }

}
