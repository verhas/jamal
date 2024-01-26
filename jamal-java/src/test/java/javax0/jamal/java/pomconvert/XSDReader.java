package javax0.jamal.java.pomconvert;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XSDReader {

    public static void readAndPrintXSD(String filePath) {
        try {
            // Create a DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XSD file
            Document document = builder.parse(filePath);

            // Normalize the XML Structure
            document.getDocumentElement().normalize();

            // Iterate through all "xs:complexType" tags
            NodeList complexTypeList = document.getElementsByTagName("xs:complexType");
            for (int i = 0; i < complexTypeList.getLength(); i++) {
                Node node = complexTypeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element complexType = (Element) node;
                    String complexTypeName = complexType.getAttribute("name");
                    createType(complexTypeName, complexType);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createType(String complexTypeName, Element complexType) {
        System.out.println("public class " + complexTypeName + "{\n");

        // Iterating through child "xs:element" tags in complex types
        NodeList childElements = complexType.getElementsByTagName("xs:element");
        for (int j = 0; j < childElements.getLength(); j++) {
            Node childNode = childElements.item(j);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                handleElement((Element) childNode);
            }
        }
        System.out.println("}\n");
    }

    private static String capitalize( String s){
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private static void handleElement(Element element) {
        String typeName = element.getAttribute("type");
        String childElementName = element.getAttribute("name");
        if( typeName.isEmpty()){
            var unnamedType = element.getElementsByTagName("xs:complexType").item(0);
            typeName = capitalize(childElementName) + "Type";
            createType(typeName, (Element) unnamedType);
        }
        if( "xs:string".equals(typeName)){
            typeName = "String";
        }
        System.out.println(typeName + " "+ childElementName + "(){}\n");
    }

    public static void main(String[] args) {
        // Replace with the path to your XSD file
        String xsdFilePath = XSDReader.class.getClassLoader().getResource("maven-4.0.0.xsd").getFile();
        readAndPrintXSD(xsdFilePath);
    }
}
