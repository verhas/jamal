package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.tools.FileTools;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class XmlDocument implements Identified, Evaluable {
    final String id;
    final Document doc;
    final XPath xPath;

    public XmlDocument(String id, String input) throws BadSyntax {
        this.id = id;
        var dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BadSyntax("Could not read or parse the XML file", e);
        }
        xPath = XPathFactory.newInstance().newXPath();
    }

    public XmlDocument(String id, Input input) throws BadSyntax {
        var reference = input.getReference();
        final var fileName = FileTools.absolute(reference, input.toString().trim());
        this.id = id;

        var dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fileName);
        } catch (Exception e) {
            throw new BadSyntax("Could not read or parse the XML file", e);
        }
        xPath = XPathFactory.newInstance().newXPath();
    }

    /**
     * Get a single string that is at the xPath in the xml.
     *
     * @param path the path we use fetching the strings
     * @return the string that can be found on the path
     * @throws XPathExpressionException if something happens during parsing
     */
    public String get(String path) throws XPathExpressionException {
        return (String) xPath.evaluate(path, doc, XPathConstants.STRING);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String evaluate(String... parameters) throws BadSyntax {
        try {
            if (parameters.length == 0) {
                return formatDocument(doc, "4");
            } else {
                return get(parameters[0]);
            }
        } catch (XPathExpressionException | TransformerException e) {
            throw new BadSyntax("The XPath expression '" + parameters[0]
                + "' on the xml document identified by '" + getId()
                + "' is erroneous", e);
        }
    }

    @Override
    public int expectedNumberOfArguments() {
        return 1;
    }

    public static String formatDocument(Document doc, String tabsize) throws TransformerException {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", tabsize);
        Writer out = new StringWriter();
        tf.transform(new DOMSource(doc), new StreamResult(out));
        return Arrays.stream(out.toString().split(System.lineSeparator())).filter(s -> s.trim().length() > 0).collect(Collectors.joining(System.lineSeparator()));
    }
}
