import javax0.jamal.api.Macro;
import javax0.jamal.snippet.Case;
import javax0.jamal.snippet.Clear;
import javax0.jamal.snippet.Collect;
import javax0.jamal.snippet.CounterMacro;
import javax0.jamal.snippet.DateMacro;
import javax0.jamal.snippet.FilesMacro;
import javax0.jamal.snippet.Format;
import javax0.jamal.snippet.Java.ClassMacro;
import javax0.jamal.snippet.Java.MethodMacro;
import javax0.jamal.snippet.KillLines;
import javax0.jamal.snippet.ListDir;
import javax0.jamal.snippet.NumberLines;
import javax0.jamal.snippet.Reflow;
import javax0.jamal.snippet.Replace;
import javax0.jamal.snippet.ReplaceLines;
import javax0.jamal.snippet.SkipLines;
import javax0.jamal.snippet.Snip;
import javax0.jamal.snippet.SnipProperties;
import javax0.jamal.snippet.SnipXml;
import javax0.jamal.snippet.Snippet;
import javax0.jamal.snippet.TrimLines;
import javax0.jamal.snippet.Update;
import javax0.jamal.snippet.XmlFormat;

module jamal.snippet {
    exports javax0.jamal.snippet;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires java.xml;
    provides Macro with Clear,
        Collect,
        NumberLines,
        Snip,
        TrimLines,
        SnipProperties,
        SnipXml,
        Replace,
        KillLines,
        SkipLines,
        Snippet,
        ReplaceLines,
        Update,
        XmlFormat,
        DateMacro,
        ListDir,
        CounterMacro,
        Case.Cap,
        Case.Decap,
        Case.Upper,
        Case.Lower,
        Format,
        ClassMacro,
        MethodMacro,
        FilesMacro.Directory,
        FilesMacro.FileMacro,
        Reflow
        ;
}