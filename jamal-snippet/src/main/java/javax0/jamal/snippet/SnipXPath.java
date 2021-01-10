package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import javax.xml.xpath.XPathExpressionException;

public class SnipXPath implements Macro, InnerScopeDependent {
    @Override
    public String getId() {
        return "snip:xpath";
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final String[] parts = InputHandler.getParts(in, 2);
        if (parts.length < 2) {
            throw new BadSyntax(getId() + " needs two parameters. The xml document id, and an XPath expression");
        }
        final var xmlDoc = processor.getRegister().getUserDefined(parts[0]).orElseThrow(
            () -> new BadSyntax("The xml document identified '" + parts[0] + "' cannot be found")
        );
        if (xmlDoc instanceof XmlDocument) {
            try {
                return ((XmlDocument) xmlDoc).get(parts[1]);
            } catch (XPathExpressionException e) {
                throw new BadSyntax("The XPath expression '" + parts[1]
                    + "' on the xml document identified by '" + parts[0]
                    + "' is erroneous", e);
            }
        }else {
            throw new BadSyntax("The macro '"+parts[0]+"' is not an xml document");
        }
    }

}
