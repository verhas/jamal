import javax0.jamal.poi.word.*;

module jamal.word {
    exports javax0.jamal.poi.word;
    requires jamal.api;
    requires jamal.engine;
    requires org.apache.poi.ooxml;
    requires jamal.tools;
    requires java.desktop;
    provides javax0.jamal.api.Macro with MacroDocxProtect, MacroDocxTrackRevisions, MacroDocxText, MacroDocxInclude, MacroDocxPicture;
}