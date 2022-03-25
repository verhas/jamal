import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

module jamal.markdown {
    exports javax0.jamal.asciidoc;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires asciidoctorj.api;
    requires asciidoctorj;
    provides ExtensionRegistry with javax0.jamal.asciidoc.JamalPreprocessor;
}