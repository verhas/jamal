package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class SnipXml implements Macro, InnerScopeDependent {
    @Override
    public String getId() {
        return "snip:xml";
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        skipWhiteSpaces(input);
        var id = fetchId(input);
        skipWhiteSpaces(input);
        if (!firstCharIs(input, '=')) {
            throw new BadSyntax(getId()+" '" + id + "' has no '=' to body");
        }
        skip(input, 1);

        final XmlDocument xmlDoc;

        if (isGlobalMacro(id)) {
            xmlDoc = new XmlDocument(convertGlobal(id),input);
            processor.defineGlobal(xmlDoc);
        } else {
            xmlDoc = new XmlDocument(id,input);
            processor.define(xmlDoc);
            // it has to be exported because it is inner scope dependent
            processor.getRegister().export(xmlDoc.getId());
        }
        return "";
    }
}
