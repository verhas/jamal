package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.tools.FileTools;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XmlDocument implements Identified, Evaluable {
    final String fileName;
    final String id;
    final Document doc;
    final XPath xPath;

    public XmlDocument(String id, Input input) throws BadSyntax {
        var reference = input.getReference();
        this.fileName = FileTools.absolute(reference, input.toString().trim());
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
     * @param path the path we use to fetch the strings
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
            return get(parameters[0]);
        } catch (XPathExpressionException e) {
            throw new BadSyntax("The XPath expression '" + parameters[0]
                + "' on the xml document identified by '" + getId()
                + "' is erroneous", e);
        }
    }

    @Override
    public int expectedNumberOfArguments() {
        return 1;
    }
}
