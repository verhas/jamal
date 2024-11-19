package javax0.jamal.snippet;

import javax0.jamal.api.*;

import static javax0.jamal.tools.InputHandler.*;

abstract class AbstractXmlDefine implements Macro, InnerScopeDependent {
    @FunctionalInterface
    interface Factory {
        XmlDocument get(String id) throws BadSyntax;
    }

    public String evaluate(Input input, Processor processor, Factory factory) throws BadSyntax {
        skipWhiteSpaces(input);
        var id = fetchId(input);
        skipWhiteSpaces(input);
        BadSyntax.when(!firstCharIs(input, '='),  "%s '%s' has no '=' to body", getId(), id);
        skip(input, 1);

        final XmlDocument xmlDoc;

        if (isGlobalMacro(id)) {
            xmlDoc = factory.get(convertGlobal(id));
            processor.defineGlobal(xmlDoc);
        } else {
            xmlDoc = factory.get(id);
            processor.define(xmlDoc);
            // it has to be exported because it is inner scope dependent
            processor.getRegister().export(xmlDoc.getId());
        }
        return "";
    }
}
