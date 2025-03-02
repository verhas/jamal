package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

@Macro.Stateful
@Macro.Name("xmlFormat")
public
class XmlFormat implements Macro, InnerScopeDependent, Scanner {

    private static final PrintStream NULL_ERR = new PrintStream(new OutputStream() {
        public void write(int b) {
            // Do nothing
        }
    });

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var tabsize = scanner.number("tabsize").defaultValue(4);
        final var thin = scanner.bool(null, "thin");
        final var wrong = scanner.bool(null, "wrong");
        scanner.done();
        skipWhiteSpaces(in);
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
        final var savedErr = System.err;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(input)));
            System.setErr(NULL_ERR);// format document sometimes vomits error to System.err when the XML is not well-formed
            return XmlDocument.formatDocument(doc, tabsize);
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            if (wrong) {
                return input;
            }
            throw new BadSyntax("There was an XML exception", e);
        } finally {
            System.setErr(savedErr);
        }
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
                skipWhiteSpaces(output);
                final var result = formatXml(output.toString(), tabsize, thin, wrong);
                output.replace(result);
            }
        }

        @Override
        public void set(Input output) {
            this.output = output;
        }
    }
}
