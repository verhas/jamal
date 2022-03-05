import javax0.jamal.poi.word.MacroDocxProtect;
import javax0.jamal.poi.word.MacroDocxTrackRevisions;

module jamal.word {
    exports javax0.jamal.poi.word;
    requires jamal.api;
    requires jamal.engine;
    requires org.apache.poi.ooxml;
    requires jamal.tools;
    requires org.apache.logging.log4j;
    provides javax0.jamal.api.Macro with MacroDocxProtect, MacroDocxTrackRevisions;
}