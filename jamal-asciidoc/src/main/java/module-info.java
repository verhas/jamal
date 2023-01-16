import javax0.jamal.asciidoc.Converter;
import javax0.jamal.asciidoc.converters.AsciiDocConverter;
import javax0.jamal.asciidoc.converters.MarkdownConverter;
import javax0.jamal.asciidoc.converters.XmlConverter;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

module jamal.asciidoc {
    exports javax0.jamal.asciidoc;
    exports javax0.jamal.asciidoc.converters;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires asciidoctorj.api;
    requires asciidoctorj;
    requires jdk.jshell;
    requires java.xml;
    requires jamal.snippet;
    requires markdown.to.asciidoc;
    provides ExtensionRegistry with javax0.jamal.asciidoc.JamalPreprocessor;
    uses Converter;
    provides Converter with AsciiDocConverter, MarkdownConverter, XmlConverter;
}