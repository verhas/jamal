import javax0.jamal.api.Macro;
import javax0.jamal.plantuml.PlantUml;

module jamal.plantuml {
    exports javax0.jamal.plantuml;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires java.xml;
    requires net.sourceforge.plantuml;
    provides Macro with PlantUml;
}