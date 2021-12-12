package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Closer;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.StringReader;

@Macro.Stateful
public class XmlFormat implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var tabsize = Params.holder("tabsize").orElseInt(4);
        final var thin = Params.holder(null, "thin").asBoolean();
        final var wrong = Params.holder(null, "wrong").asBoolean();
        Params.using(processor).from(this).between("()").keys(tabsize, thin, wrong).parse(in);


        InputHandler.skipWhiteSpaces(in);
        if (in.length() > 0) {
            final String input = in.toString();
            return formatXml(input, "" + tabsize.get(), thin.is(), wrong.is());
        } else {
            final var it = new XmlFormatCloser(tabsize.get(), thin.is(), wrong.is());
            processor.deferredClose(it);
            return "";
        }
    }

    private static String formatXml(String input, String tabsize, boolean thin, boolean wrong) throws BadSyntax {
        if (thin) {
            input = new ThinXml(input).getXml();
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(input)));
            return XmlDocument.formatDocument(doc, tabsize);
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            if (wrong) {
                return input;
            }
            throw new BadSyntax("There was an XML exception", e);
        }
    }

    @Override
    public String getId() {
        return "xmlFormat";
    }

    private static class XmlFormatCloser implements Closer.OutputAware, AutoCloseable {

        private Input output = null;
        private final String tabsize;
        private final boolean thin;
        private final boolean wrong;

        private XmlFormatCloser(final int tabsize, boolean thin, boolean wrong) {
            this.tabsize = "" + tabsize;
            this.thin = thin;
            this.wrong = wrong;
        }

        @Override
        public boolean equals(Object o) {
            return XmlFormatCloser.class == o.getClass();
        }

        @Override
        public int hashCode() {
            return XmlFormatCloser.class.hashCode();
        }

        @Override
        public void close() throws BadSyntax {
            if (output != null) {
                InputHandler.skipWhiteSpaces(output);
                final var result = formatXml(output.toString(), tabsize, thin, wrong);
                output.getSB().delete(0, output.getSB().length());
                output.getSB().append(result);
            }
        }

        @Override
        public void set(Input output) {
            this.output = output;
        }
    }
}
