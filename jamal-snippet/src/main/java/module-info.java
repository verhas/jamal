import javax0.jamal.api.Macro;
import javax0.jamal.snippet.*;
import javax0.jamal.snippet.Java.ClassMacro;
import javax0.jamal.snippet.Java.FieldMacro;
import javax0.jamal.snippet.Java.MethodMacro;
import javax0.jamal.snippet.StringMacros.Chop;
import javax0.jamal.snippet.StringMacros.Contains;
import javax0.jamal.snippet.StringMacros.EndsWith;
import javax0.jamal.snippet.StringMacros.Equals;
import javax0.jamal.snippet.StringMacros.Length;
import javax0.jamal.snippet.StringMacros.Quote;
import javax0.jamal.snippet.StringMacros.Reverse;
import javax0.jamal.snippet.StringMacros.StartsWith;
import javax0.jamal.snippet.StringMacros.Substring;

module jamal.snippet {
    exports javax0.jamal.snippet;
    exports javax0.jamal.snippet.tools;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires java.xml;
    requires javaLex;
    requires SourceBuddy;
    requires jdk.compiler;
    requires refi;
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
            Chop,
            EndsWith,
            Sort,
            SnipLine,
            SnipFile,
            Untab,
            RangeMacro,
            Rot13,
            Pos,
            Numbers,
            Unicode,
            Base64.Encode,
            Base64.Decode,
            Reference,
            References,
            SnipTransform,
            JavaSourceInsert,
            JavaSourceTemplate,
            JavaMatcherBuilderMacros.AnyTill,
            JavaMatcherBuilderMacros.Not,
            JavaMatcherBuilderMacros.Identifier,
            JavaMatcherBuilderMacros.StringMacro,
            JavaMatcherBuilderMacros.List,
            JavaMatcherBuilderMacros.Comment,
            JavaMatcherBuilderMacros.OptionalMacro,
            JavaMatcherBuilderMacros.Keyword,
            JavaMatcherBuilderMacros.OneOrMore,
            JavaMatcherBuilderMacros.Unordered,
            JavaMatcherBuilderMacros.ZeroOrMore,
            JavaMatcherBuilderMacros.OneOf,
            JavaMatcherBuilderMacros.CharacterMacro,
            JavaMatcherBuilderMacros.IntegerMacro,
            JavaMatcherBuilderMacros.NumberMacro,
            JavaMatcherBuilderMacros.FloatMacro,
            JavaMatcherBuilderMacros.Match,
            CompileJavaMacros.Compile,
            CompileJavaMacros.ListClasses,
            CompileJavaMacros.ListMethods,
            CompileJavaMacros.ListFields,
            ShellVar,
            HashCode,
            Memoize,
            Download,
            UrlEncode,
            Decorate,
            Dictionary,
            Repeat
            ;
}