import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;
import javax0.jamal.asciidoc.*;
import javax0.jamal.asciidoc.converters.*;

module jamal.asciidoc {
    exports javax0.jamal.asciidoc;
    exports javax0.jamal.asciidoc.converters;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires jamal.asciidoc_COMPAT;
    requires jdk.jshell;
    requires java.xml;
    requires jamal.snippet;
    requires markdown.to.asciidoc;
    requires asciidoctorj;
    requires asciidoctorj.api;
    provides ExtensionRegistry with javax0.jamal.asciidoc.JamalPreprocessor;
    uses Converter;
    provides Converter with AsciiDocConverter, MarkdownConverter, XmlConverter;
}