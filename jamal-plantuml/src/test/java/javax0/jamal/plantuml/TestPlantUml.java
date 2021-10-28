package javax0.jamal.plantuml;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestPlantUml {

    @Test
    @DisplayName("Create a simple test diagram")
    void createSimpleDiagram() throws Exception {
        TestThat.theInput(
            "{#plantuml (folder=target/) myfirstdiagram1.svg\n" +
                "Bob -> Alice : hello\n" +
                "}"
        ).results("myfirstdiagram1.svg");
    }

    @Test
    @DisplayName("Create a simple diagram with pre and postamble")
    void createSimpleDiagramWithPreAndPostamble() throws Exception {
        TestThat.theInput(
            "{#plantuml (folder=target/) myfirstdiagram2.svg\n" +
                "@startuml\n" +
                "Bob -> Alice : hello\n" +
                "@enduml\n" +
                "}"
        ).results("myfirstdiagram2.svg");
    }

    @Test
    @DisplayName("Create a simple PNG diagram using macros to define the output directory and the format")
    void createSimpleDiagramPNG() throws Exception {
        TestThat.theInput(
            "{#plantuml myfirstdiagram3.png\n" +
                "@startuml\n" +
                "{@define pu$format=PNG}" +
                "{@define pu$folder=target/}" +
                "Bob -> Alice : hello\n" +
                "@enduml\n" +
                "}"
        ).results("myfirstdiagram3.png");
    }

    @Test
    @DisplayName("Create a simple PNG diagram using option to define the output directory and the format")
    void createSimpleDiagramWithOptionsPNG() throws Exception {
        TestThat.theInput(
            "{#plantuml (format=PNG folder=target/) myfirstdiagram4.png\n" +
                "@startuml\n" +
                "Bob -> Alice : hello\n" +
                "@enduml\n" +
                "}"
        ).results("myfirstdiagram4.png");
    }

    @Test
    @DisplayName("Create a simple PNG diagram using option to define the format only")
    void createSimpleDiagramWithOptionsPNGNoFolder() throws Exception {
        final String fn = "myfirstdiagram1.svg";
        TestThat.theInput(
            "{#plantuml " + fn + "\n" +
                "Bob -> Alice : hello\n" +
                "}"
        ).results(fn);
        new File(fn).deleteOnExit();
    }

    @Test
    @DisplayName("Try to create an erroneous PlantUML diagram, it should throw an error")
    void throwForErroneousInput() throws Exception {
        TestThat.theInput(
            "{#plantuml erroneous.svg\n" +
                "{@define pu$folder=target/}" +
                "sasds dsff ds ds  sdf dsf \n" +
                "}"
        ).throwsBadSyntax("There was an error processing diagram for 'erroneous\\.svg' in PlantUml\\.");
    }
}
