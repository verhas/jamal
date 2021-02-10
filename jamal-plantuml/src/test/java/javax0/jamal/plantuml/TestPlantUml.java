package javax0.jamal.plantuml;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestPlantUml {

    @Test
    void createSimpleDiagram() throws Exception {
        TestThat.theInput(
            "{#plantuml myfirstdiagram.svg\n" +
                "{@define pu$folder=target/}" +
                "Bob -> Alice : hello\n" +
                "}"
        ).results("myfirstdiagram.svg");
    }

    @Test
    void createSimpleDiagramWithPreAndPostamble() throws Exception {
        TestThat.theInput(
            "{#plantuml myfirstdiagram.svg\n" +
                "@startuml\n" +
                "{@define pu$folder=target/}" +
                "Bob -> Alice : hello\n" +
                "@enduml\n" +
                "}"
        ).results("myfirstdiagram.svg");
    }

    @Test
    void createSimpleDiagramPNG() throws Exception {
        TestThat.theInput(
            "{#plantuml myfirstdiagram.png\n" +
                "@startuml\n" +
                "{@define pu$format=PNG}" +
                "{@define pu$folder=target/}" +
                "Bob -> Alice : hello\n" +
                "@enduml\n" +
                "}"
        ).results("myfirstdiagram.png");
    }

    @Test
    void throwForErroneousInput() throws Exception {
        TestThat.theInput(
            "{#plantuml erroneous.svg\n" +
                "{@define pu$folder=target/}" +
                "sasds dsff ds ds  sdf dsf \n" +
                "}"
        ).throwsBadSyntax();
    }
}
