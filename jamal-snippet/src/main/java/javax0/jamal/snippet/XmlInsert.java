package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

@Macro.Name("xml:insert")
public class XmlInsert implements Macro, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var pos = in.getPosition();
        final var scanner = newScanner(in, processor);
        final var xpath = scanner.str("xpath", "path");
        final var into = scanner.str("id", "to", "into").optional();
        final var needed = scanner.bool("ifneeded", "optional");
        final var tabsize = scanner.number("tabsize").defaultValue(4);
        scanner.done();
        skipWhiteSpaces(in);
        final var xml = in.toString();
        BadSyntax.when(!xpath.isPresent(),  "The 'xpath' parameter is mandatory in '%s'", getId());
        final var xpathString = xpath.get();
        if (into.isPresent()) {
            final var intoDocument = processor.getRegister().getUserDefined(into.get())
                .filter(s -> s instanceof XmlDocument)
                .map(XmlDocument.class::cast)
                .orElseThrow(() -> new BadSyntax("The value of the 'into' parameter must be an instance of XmlDocument")).doc;
            try {
                insert(intoDocument, xpathString, xml, needed.is());
            } catch (Exception e) {
                throw new BadSyntaxAt("Cannot insert XML into '" + into.get() + "' ", in.getPosition(), e);
            }
        } else {
            final var closer = (XmlInsertCloser) processor.deferredClose(new XmlInsertCloser());
            closer.add(xml, xpathString, needed.is(), pos);
            if (tabsize.isPresent()) {
                closer.tabsize = tabsize.get();
            }
        }
        return "";
    }

    static void insert(Document intoDocument, String xpath, String xml, boolean need) throws Exception {
        final var dbFactory = DocumentBuilderFactory.newInstance();
        final var dBuilder = dbFactory.newDocumentBuilder();
        final var doc = dBuilder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        final var xPathEngine = XPathFactory.newInstance().newXPath();
        final var node = (Node) xPathEngine.evaluate(xpath, intoDocument, XPathConstants.NODE);
        if( node == null ){
            throw new BadSyntax("The XPath expression '"+xpath+"' did not match any node in the document.");
        }
        final var importNode = intoDocument.importNode(doc.getFirstChild(), true);
        if (need) {
            final var name = importNode.getNodeName();
            final var children = node.getChildNodes();
            final var nrChilds = children.getLength();
            for (int i = 0; i < nrChilds; i++) {
                if (children.item(i).getNodeName().equals(name)) {
                    return;
                }
            }
        }
        node.appendChild(importNode);
    }

    private static class XmlInsertCloser implements AutoCloseable, Closer.OutputAware {
        static class ToBeInserted {
            final String xml;
            final String xpath;
            final boolean needed;
            final Position pos;

            private ToBeInserted(String xml, String xpath, boolean needed, Position pos) {
                this.xml = xml;
                this.xpath = xpath;
                this.needed = needed;
                this.pos = pos;
            }
        }

        private int tabsize = 4;
        private List<ToBeInserted> insert = null;
        private Input output;

        public void add(String xml, String xpath, boolean needed, Position pos) {
            if (insert == null) {
                insert = new ArrayList<>();
            }
            insert.add(new ToBeInserted(xml, xpath, needed, pos));
        }

        @Override
        public boolean equals(Object o) {
            return XmlInsertCloser.class == o.getClass();
        }

        @Override
        public int hashCode() {
            return XmlInsertCloser.class.hashCode();
        }

        @Override
        public void close() throws BadSyntax {
            try {
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setValidating(false);
                final DocumentBuilder db = dbf.newDocumentBuilder();
                final Document doc = db.parse(new InputSource(new StringReader(output.toString())));
                for (final var toBeInserted : insert) {
                    try {
                        insert(doc, toBeInserted.xpath, toBeInserted.xml, toBeInserted.needed);
                    } catch (Exception e) {
                        throw new BadSyntaxAt("Cannot insert XML at '" + toBeInserted.xpath + "'", toBeInserted.pos, e);
                    }
                }
                final var result = XmlDocument.formatDocument(doc, "" + tabsize);
                output.getSB().setLength(0);
                output.getSB().append(result);
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                throw new BadSyntax("Error while inserting XML nodes into the final document.", e);
            }
        }

        @Override
        public void set(Input output) {
            this.output = output;
        }
    }

}
