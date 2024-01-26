package javax0.jamal.java.testmacros;

import javax0.jamal.java.Xml;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static javax0.jamal.java.Xml.path;
import static javax0.jamal.java.Xml.tagValue;

public class TestXml {

    @Test
    void testTagCreation(){
        final Xml xml = Xml.tagValue("project", null);
        Assertions.assertEquals("<project/>",xml.toString());
        xml.add(path("project","dependencies"),null);
        Assertions.assertEquals("<project><dependencies/></project>",xml.toString());
        //xml.add(path("project","dependencies", "dependency"),"depi");
        xml.add(path("project","dependencies"),tagValue("dependency", "depi"));
        Assertions.assertEquals("<project><dependencies><dependency>depi</dependency></dependencies></project>",xml.toString());
        xml.add(path("project","dependencies"),tagValue("dependency", "pumba"));
        Assertions.assertEquals("<project><dependencies><dependency>depi</dependency><dependency>pumba</dependency></dependencies></project>",xml.toString());
    }

}
