package javax0.jamal.snippet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestThinXml {

    @Test
    @DisplayName("Empty string thin XML is empty string")
    void testEmptyString() throws Exception {
        Assertions.assertEquals("", new ThinXml("").getXml());
    }

    @Test
    @DisplayName("Converts a single tag successfully")
    void testOneTag() throws Exception {
        Assertions.assertEquals("<apple>\n</apple>\n"
            , new ThinXml("" +
                "apple>"
            ).getXml());
    }

    @Test
    @DisplayName("Converts two tags on a single line successfully")
    void testTwoTagOnOneLine() throws Exception {
        Assertions.assertEquals("" +
                "<apple>\n" +
                "    <plum>\n" +
                "    </plum>\n" +
                "</apple>\n"
            , new ThinXml("" +
                "apple>plum>"
            ).getXml());
    }

    @Test
    @DisplayName("Converts two tags on a single line with attributes successfully")
    void testTwoTagOnOneLineAttr() throws Exception {
        Assertions.assertEquals("" +
                "<apple core=\"hard\" chew=\"bakka\">\n" +
                "    <plum>\n" +
                "    </plum>\n" +
                "</apple>\n"
            , new ThinXml("" +
                "apple core=hard chew=\"bakka\">plum>"
            ).getXml());
    }

    @Test
    @DisplayName("One helluva complex test")
    void testSomethingComplex() throws Exception {
        Assertions.assertEquals("" +
                "<apple core=\"hard\" chew=\"bak&quot;ka\">\n" +
                "    <plum>\n" +
                "        this is just text &gt; under plum\n" +
                "    </plum>\n" +
                "    here plum is closed and this is another text\n" +
                "\n" +
                "    <pineApple>is another fine fruit that goes well with pizz...aaarghhh<![CDATA[\n" +
                "    this is a CDATA section\n" +
                " a few lines\n" +
                "  ever more tabbed\n" +
                "and untabbed\n" +
                "]]>\n" +
                "</pineApple>\n" +
                "</apple>\n"
            , new ThinXml("" +
                "apple core=hard chew=\"bak\\\"ka\">\n" +
                "        plum>\n" +
                "          this is just text &gt; under plum\n" +
                "        here plum is closed and this is another text\n" +
                "\n" + // an empty line
                "     pineApple>is another fine fruit that goes well with pizz...aaarghhh\n" +
                "         <![CDATA[\n" +
                "    this is a CDATA section\n a few lines\n  ever more tabbed\nand untabbed\n" +
                "]]>\n"
            ).getXml());
    }

    @Test
    @DisplayName("One one liners")
    void testOneLiners() throws Exception {
        Assertions.assertEquals("" +
                "<project>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <name>jamal snippet</name>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <groupId>com.javax0.jamal</groupId>\n" +
                "    <artifactId>jamal-snippet</artifactId>\n" +
                "    <version>{%VERSION%}</version>\n" +
                "    <parent>\n" +
                "        <groupId>com.javax0.jamal</groupId>\n" +
                "        <artifactId>jamal-parent</artifactId>\n" +
                "        <version>1.10.3-SNAPSHOT</version>\n" +
                "        <relativePath>../jamal-parent</relativePath>\n" +
                "    </parent>\n" +
                "</project>\n"
            , new ThinXml("" +
                "project>\n" +
                "  modelVersion>4.0.0\n" +
                "  name>jamal snippet\n" +
                "  packaging>jar\n" +
                "  groupId>com.javax0.jamal\n" +
                "  artifactId>jamal-snippet\n" +
                "  version>{%VERSION%}\n" +
                "  parent>\n" +
                "    groupId>com.javax0.jamal\n" +
                "    artifactId>jamal-parent\n" +
                "    version>1.10.3-SNAPSHOT\n" +
                "    relativePath>../jamal-parent"
            ).getXml());
    }

    @Test
    @DisplayName("One one liners with included full XML")
    void testOneLinersWithXml() throws Exception {
        Assertions.assertEquals("" +
                "<project>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <name>jamal snippet</name>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <groupId>com.javax0.jamal</groupId>\n" +
                "    <artifactId>jamal-snippet</artifactId>\n" +
                "    <version>{%VERSION%}</version>\n" +
                "<parent>\n" +
                "    <groupId>com.javax0.jamal</groupId>\n" +
                "    <artifactId>jamal-parent</artifactId>\n" +
                "    <version>1.10.3-SNAPSHOT</version>\n" +
                "    <relativePath>../jamal-parent</relativePath>\n" +
                "</parent>\n" +
                "</project>\n" +
                "<ohhh></ohhh>\n"
            , new ThinXml("" +
                "project>\n" +
                "  modelVersion>4.0.0\n" +
                "  name>jamal snippet\n" +
                "  packaging>jar\n" +
                "  groupId>com.javax0.jamal\n" +
                "  artifactId>jamal-snippet\n" +
                "  version>{%VERSION%}\n" +
                "<parent>\n" +
                "groupId>com.javax0.jamal\n" +
                "artifactId>jamal-parent\n" +
                "version>1.10.3-SNAPSHOT\n" +
                "relativePath>../jamal-parent\n" +
                "</parent>\n" +
                "<ohhh></ohhh>\n"
            ).getXml());
    }

    @Test
    @DisplayName("One one liners with included full XML")
    void testAnotherOneLinersWithXml() throws Exception {
        Assertions.assertEquals("" +
                "<project>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <name>jamal snippet</name>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <profiles>\n" +
                "        <profile>\n" +
                "            <id>release</id>\n" +
                "            <build>\n" +
                "                <plugins>\n" +
                "         <plugin>\n" +
                "             <groupId>org.apache.maven.plugins</groupId>\n" +
                "             <artifactId>maven-gpg-plugin</artifactId>\n" +
                "             <version>3.0.1</version>\n" +
                "             <executions>\n" +
                "                 <execution>\n" +
                "                     <id>sign-artifacts</id>\n" +
                "                     <phase>verify</phase>\n" +
                "                     <goals>\n" +
                "                         <goal>sign</goal>\n" +
                "                     </goals>\n" +
                "                 </execution>\n" +
                "             </executions>\n" +
                "         </plugin>\n" +
                "                </plugins>\n" +
                "            </build>\n" +
                "        </profile>\n" +
                "    </profiles>\n" +
                "</project>\n"
            , new ThinXml("" +
                "project>\n" +
                "       modelVersion>4.0.0\n" +
                "       name>jamal snippet\n" +
                "       packaging>jar\n" +
                "       profiles>profile>\n" +
                "                  id>release\n" +
                "                  build>plugins>\n" +
                "         <plugin>\n" +
                "           groupId>org.apache.maven.plugins\n" +
                "           artifactId>maven-gpg-plugin\n" +
                "           version>3.0.1\n" +
                "           executions>execution>\n" +
                "                        id>sign-artifacts\n" +
                "                        phase>verify\n" +
                "                        goals>goal>sign\n" +
                "         </plugin>\n"
            ).getXml());
    }
}
