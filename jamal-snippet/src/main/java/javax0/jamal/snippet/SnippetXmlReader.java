package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.SHA256;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import javax0.jamal.tools.Format;

public class SnippetXmlReader {

    /**
     * Read the snippet from the given input stream. The input stream should contain the XML formatted snippet store.
     *
     * @param is       the input stream
     * @param consumer the consumer to which the snippet is passed
     * @throws BadSyntax if the XML is not well-formed
     */
    public static void getSnippetsFromXml(InputStream is,
                                          SnippetConsumer consumer) throws BadSyntax {
        try {
            final var root = getSnippetsRoot(is);
            BadSyntax.when(is(root, "snippets"), Format.msg("The root element of the XML document must be <snippets xmlns=\"%s\">", SnipSave.NS));
            final var snippets = root.getChildNodes();
            for (int i = 0; i < snippets.getLength(); i++) {
                final var snippet = snippets.item(i);
                if (isProcessable(snippet)) {
                    BadSyntax.when(is(snippet, "snippet"),
                            "XML document must contain only 'snippet' tags under the 'snippets' root element");
                    final var id = getStringAttribute(snippet, "id", null);
                    final var fnValue = getStringAttribute(snippet, "file", id);
                    final var lineValue = getIntAttribute(snippet, "line", id);
                    final var columnValue = getIntAttribute(snippet, "column", id);
                    final String text = slurpCDATASections(snippet, id);

                    checkHash(snippet, id, text);

                    consumer.appy(id, text, new Position(fnValue, lineValue, columnValue));
                }
            }
        } catch (Exception e) {
            throw new BadSyntax("Could not read or parse the XML file", e);
        }
    }

    /**
     * Check the hash of the text. If the calculated hash does not match the hash in the XML document,
     * throw an exception.
     *
     * @param snippet the snippet node
     * @param id      the id of the snippet used only for error reporting
     * @param text    the text of the snippet used to calculate the hash
     * @throws BadSyntax if the hash does not match
     */
    private static void checkHash(Node snippet, String id, String text) throws BadSyntax {
        final var badHash =
                Optional.ofNullable(snippet.getAttributes().getNamedItem("hash"))
                        .map(Node::getNodeValue)
                        .filter(hash -> !Objects.equals(hash, SnipCheck.doted(HexDumper.encode(SHA256.digest(text)))));
        BadSyntax.when(badHash.isPresent(), Format.msg("The 'hash' attribute of the 'snippet id=%s' tag must be equal to the hash of the text", id));
    }

    /**
     * Get the root element of the XML document.
     *
     * @param is the input stream containing the XML document as text
     * @return the root element of the XML document
     * @throws ParserConfigurationException if the XML document is not well-formed
     * @throws SAXException                 if the XML document is not well-formed
     * @throws IOException                  if the XML document is not well-formed
     */
    private static Node getSnippetsRoot(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        final var dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        final var dBuilder = dbFactory.newDocumentBuilder();
        final var doc = dBuilder.parse(is);
        return doc.getDocumentElement();
    }

    private static boolean is(Node root, String localName) {
        return Document.ELEMENT_NODE != root.getNodeType() ||
                !localName.equals(root.getLocalName()) ||
                !SnipSave.NS.equals(root.getNamespaceURI());
    }

    private static boolean isProcessable(Node node) {
        return (Document.TEXT_NODE != node.getNodeType() ||
                !node.getTextContent().trim().isEmpty())
                && Document.COMMENT_NODE != node.getNodeType();
    }

    /**
     * Slurp all CDATA sections and return the concatenated text. Ignore the interleaving text nodes.
     *
     * @param id   the identifier of the snippet tag, used only for error messages
     * @param node to be scanned for CDATA children
     * @return the concatenated text of all CDATA sections
     * @throws BadSyntax if there is no CDATA section, or there is some node, which is neither a text node nor a CDATA section
     */
    private static String slurpCDATASections(Node node, String id) throws BadSyntax {
        final var texts = node.getChildNodes();
        final var sb = new StringBuilder();
        int countTexts = 0;
        for (int j = 0; j < texts.getLength(); j++) {
            final var text = texts.item(j);
            if (isProcessable(text)) {
                if (Document.CDATA_SECTION_NODE == text.getNodeType()) {
                    sb.append(text.getNodeValue());
                    countTexts++;
                }
            }
        }
        BadSyntax.when(countTexts == 0, Format.msg("The 'snippet id=%s' tag must have at least one CDATA section", id));
        return sb.toString();
    }


    private static String getStringAttribute(final Node node, final String attribute, final String id) throws BadSyntax {
        return Optional.ofNullable(node.getAttributes().getNamedItem(attribute)).map(Node::getNodeValue).orElseThrow(
                () -> new BadSyntax(String.format("The 'snippet%s' tag must have an '%s' attribute", id == null ? "" : " id=" + id, attribute)));
    }

    private static int getIntAttribute(final Node node, final String attribute, final String id) throws BadSyntax {
        try {
            return Integer.parseInt(getStringAttribute(node, attribute, id));
        } catch (NumberFormatException e) {
            throw new BadSyntax("The '" + attribute + "' attribute of the 'snippet id=" + id + "' tag must be an integer");
        }
    }

    public interface SnippetConsumer {
        void appy(String id, String content, Position position) throws BadSyntax;
    }
}
