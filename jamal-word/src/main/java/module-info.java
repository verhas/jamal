import javax0.jamal.poi.word.MacroDocxInclude;
import javax0.jamal.poi.word.MacroDocxPicture;
import javax0.jamal.poi.word.MacroDocxProtect;
import javax0.jamal.poi.word.MacroDocxText;
import javax0.jamal.poi.word.MacroDocxTrackRevisions;

module jamal.word {
    exports javax0.jamal.poi.word;
    requires jamal.api;
    requires jamal.engine;
    requires org.apache.poi.ooxml;
    requires jamal.tools;
    requires java.desktop;
    provides javax0.jamal.api.Macro with MacroDocxProtect, MacroDocxTrackRevisions, MacroDocxText, MacroDocxInclude, MacroDocxPicture;
}