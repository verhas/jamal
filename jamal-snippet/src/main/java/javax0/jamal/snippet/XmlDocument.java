package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XmlDocument implements Identified {
    final String fileName;
    final String id;
    final Document doc;
    final XPath xPath;

    public XmlDocument(String id,String fileName) throws BadSyntax {
        this.fileName = fileName;
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
}
