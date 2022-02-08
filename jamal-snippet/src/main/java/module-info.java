import javax0.jamal.api.Macro;
import javax0.jamal.snippet.Case;
import javax0.jamal.snippet.Clear;
import javax0.jamal.snippet.Collect;
import javax0.jamal.snippet.CounterMacro;
import javax0.jamal.snippet.DateMacro;
import javax0.jamal.snippet.FilesMacro;
import javax0.jamal.snippet.Format;
import javax0.jamal.snippet.Java.ClassMacro;
import javax0.jamal.snippet.Java.FieldMacro;
import javax0.jamal.snippet.Java.MethodMacro;
import javax0.jamal.snippet.KillLines;
import javax0.jamal.snippet.LineCount;
import javax0.jamal.snippet.ListDir;
import javax0.jamal.snippet.NumberLines;
import javax0.jamal.snippet.RangeMacro;
import javax0.jamal.snippet.Reflow;
import javax0.jamal.snippet.Replace;
import javax0.jamal.snippet.ReplaceLines;
import javax0.jamal.snippet.Rot13;
import javax0.jamal.snippet.SkipLines;
import javax0.jamal.snippet.Snip;
import javax0.jamal.snippet.SnipCheck;
import javax0.jamal.snippet.SnipFile;
import javax0.jamal.snippet.SnipLine;
import javax0.jamal.snippet.SnipList;
import javax0.jamal.snippet.SnipLoad;
import javax0.jamal.snippet.SnipProperties;
import javax0.jamal.snippet.SnipSave;
import javax0.jamal.snippet.SnipTransform;
import javax0.jamal.snippet.SnipXml;
import javax0.jamal.snippet.Snippet;
import javax0.jamal.snippet.StringMacros.Contains;
import javax0.jamal.snippet.StringMacros.EndsWith;
import javax0.jamal.snippet.StringMacros.Equals;
import javax0.jamal.snippet.StringMacros.Length;
import javax0.jamal.snippet.StringMacros.Quote;
import javax0.jamal.snippet.StringMacros.Reverse;
import javax0.jamal.snippet.StringMacros.StartsWith;
import javax0.jamal.snippet.StringMacros.Substring;
import javax0.jamal.snippet.ThinXmlMacro;
import javax0.jamal.snippet.TrimLines;
import javax0.jamal.snippet.Untab;
import javax0.jamal.snippet.Update;
import javax0.jamal.snippet.Xml;
import javax0.jamal.snippet.XmlFormat;
import javax0.jamal.snippet.XmlInsert;

module jamal.snippet {
    exports javax0.jamal.snippet;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires java.xml;
    provides Macro with Clear,
            Collect,
            LineCount,
            NumberLines,
            Snip,
            SnipCheck,
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
            ThinXmlMacro,
            XmlInsert,
            Xml,
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
            FieldMacro,
            FilesMacro.Directory,
            FilesMacro.FileMacro,
            Reflow,
            SnipList,
            SnipSave,
            SnipLoad,
            Contains,
            Substring,
            Length,
            Quote,
            Equals,
            StartsWith,
            Reverse,
            EndsWith,
            SnipLine,
            SnipFile,
            Untab,
            RangeMacro,
            Rot13,
            SnipTransform;
}