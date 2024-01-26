package javax0.jamal.java.testmacros;

import javax0.jamal.java.Xml;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class TestJdsl {

    //@Test
    void testJdslSample() throws Exception {
        final String input;
        final var resource =ClassLoader.getSystemResource("pom.java");
        try( final var is = resource.openStream() ){
            input = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        TestThat.theInput(input).atPosition(resource.toURI().toURL().getFile(),1,1).ignoreLineEnding()
                .results("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                        "<project>\n" +
                        "  <modelVersion>4.0.0</modelVersion>\n" +
                        "  <dependencyManagement>\n" +
                        "    <dependencies>\n" +
                        "      <dependency>\n" +
                        "        <groupId>myGroup</groupId>\n" +
                        "        <artifactId>myArtifact</artifactId>\n" +
                        "        <version>1.0.0</version>\n" +
                        "      </dependency>\n" +
                        "      <dependency>\n" +
                        "        <groupId>org.junit.jupiter</groupId>\n" +
                        "        <artifactId>junit-jupiter-api</artifactId>\n" +
                        "        <version>5.9.0</version>\n" +
                        "        <scope>test</scope>\n" +
                        "        <exclusions>\n" +
                        "          <exclusion>\n" +
                        "            <groupId>grp</groupId>\n" +
                        "            <artifactId>arti</artifactId>\n" +
                        "          </exclusion>\n" +
                        "          <exclusion>\n" +
                        "            <groupId>grp</groupId>\n" +
                        "            <artifactId>arti</artifactId>\n" +
                        "          </exclusion>\n" +
                        "        </exclusions>\n" +
                        "      </dependency>\n" +
                        "      <dependency>\n" +
                        "        <groupId>org.junit.jupiter</groupId>\n" +
                        "        <artifactId>junit-jupiter-api</artifactId>\n" +
                        "        <version>5.9.1</version>\n" +
                        "      </dependency>\n" +
                        "      <dependency>\n" +
                        "        <groupId>org.junit.jupiter</groupId>\n" +
                        "        <artifactId>junit-jupiter-api</artifactId>\n" +
                        "        <version>5.7.0</version>\n" +
                        "        <type>jar</type>\n" +
                        "        <scope>test</scope>\n" +
                        "      </dependency>\n" +
                        "      <dependency>\n" +
                        "        <groupId>org.junit.jupiter</groupId>\n" +
                        "        <artifactId>junit-jupiter-api</artifactId>\n" +
                        "        <scope>test</scope>\n" +
                        "        <version>5.7.0</version>\n" +
                        "        <type>jar</type>\n" +
                        "      </dependency>\n" +
                        "    </dependencies>\n" +
                        "  </dependencyManagement>\n" +
                        "  <parent>\n" +
                        "    <groupId>myGroup</groupId>\n" +
                        "    <artifactId>myParent</artifactId>\n" +
                        "    <version>1.0.0</version>\n" +
                        "    <relativePath>../myParent</relativePath>\n" +
                        "  </parent>\n" +
                        "  <modules>\n" +
                        "    <module>module1</module>\n" +
                        "  </modules>\n" +
                        "  <packaging>pom</packaging>\n" +
                        "  <description>This is a test project</description>\n" +
                        "  <dependencies>\n" +
                        "    <dependency>\n" +
                        "      <groupId>org.junit.jupiter</groupId>\n" +
                        "      <artifactId>junit-jupiter-api</artifactId>\n" +
                        "      <version>5.9.0</version>\n" +
                        "      <scope>test</scope>\n" +
                        "      <exclusions>\n" +
                        "        <exclusion>\n" +
                        "          <groupId>grp</groupId>\n" +
                        "          <artifactId>arti</artifactId>\n" +
                        "        </exclusion>\n" +
                        "        <exclusion>\n" +
                        "          <groupId>grp</groupId>\n" +
                        "          <artifactId>arti</artifactId>\n" +
                        "        </exclusion>\n" +
                        "      </exclusions>\n" +
                        "    </dependency>\n" +
                        "    <dependency>\n" +
                        "      <groupId>org.junit.jupiter</groupId>\n" +
                        "      <artifactId>junit-jupiter-api</artifactId>\n" +
                        "      <version>5.9.1</version>\n" +
                        "    </dependency>\n" +
                        "    <dependency>\n" +
                        "      <groupId>org.junit.jupiter</groupId>\n" +
                        "      <artifactId>junit-jupiter-api</artifactId>\n" +
                        "      <version>5.7.0</version>\n" +
                        "      <type>jar</type>\n" +
                        "      <scope>test</scope>\n" +
                        "    </dependency>\n" +
                        "    <dependency>\n" +
                        "      <groupId>org.junit.jupiter</groupId>\n" +
                        "      <artifactId>junit-jupiter-api</artifactId>\n" +
                        "      <scope>test</scope>\n" +
                        "      <version>5.7.0</version>\n" +
                        "      <type>jar</type>\n" +
                        "    </dependency>\n" +
                        "    <dependency>\n" +
                        "      <groupId>gid</groupId>\n" +
                        "      <artifactId>aid</artifactId>\n" +
                        "      <version>ver</version>\n" +
                        "      <scope>scope</scope>\n" +
                        "      <classifier>classifier</classifier>\n" +
                        "      <type>type</type>\n" +
                        "    </dependency>\n" +
                        "    <dependency>\n" +
                        "      <groupId>tag-groupId</groupId>\n" +
                        "      <artifactId>tag-artifactId</artifactId>\n" +
                        "      <version>tag-version</version>\n" +
                        "      <scope>tag-scope</scope>\n" +
                        "      <classifier>tag-classifier</classifier>\n" +
                        "      <type>tag-type</type>\n" +
                        "    </dependency>\n" +
                        "  </dependencies>\n" +
                        "  <build>\n" +
                        "    <resources>\n" +
                        "      <resource>\n" +
                        "        <targetPath>pom.xml</targetPath>\n" +
                        "        <filtering>true</filtering>\n" +
                        "        <directory>abraka dabra</directory>\n" +
                        "      </resource>\n" +
                        "    </resources>\n" +
                        "    <filters>\n" +
                        "      <filter>filter1</filter>\n" +
                        "      <filter>filter2</filter>\n" +
                        "    </filters>\n" +
                        "    <defaultGoal>clean</defaultGoal>\n" +
                        "    <finalName>alma-ata</finalName>\n" +
                        "  </build>\n" +
                        "  <organization>\n" +
                        "    <organization>\n" +
                        "      <name>javax0</name>\n" +
                        "      <url>http://javax0.com</url>\n" +
                        "    </organization>\n" +
                        "  </organization>\n" +
                        "  <url>http://example.com</url>\n" +
                        "  <licenses>\n" +
                        "    <license>\n" +
                        "      <name>MIT License</name>\n" +
                        "      <url>https://opensource.org/licenses/MIT</url>\n" +
                        "      <distribution>repo</distribution>\n" +
                        "    </license>\n" +
                        "  </licenses>\n" +
                        "  <developers>\n" +
                        "    <id>PV</id>\n" +
                        "    <name>Peter Verhas</name>\n" +
                        "    <email>peter@verhas.com</email>\n" +
                        "    <organization>N/A</organization>\n" +
                        "    <roles>\n" +
                        "      <role>developer</role>\n" +
                        "    </roles>\n" +
                        "    <timezone>GMT+1</timezone>\n" +
                        "  </developers>\n" +
                        "  <properties>\n" +
                        "    <key.zapp>value</key.zapp>\n" +
                        "  </properties>\n" +
                        "</project>\n");
    }
}
