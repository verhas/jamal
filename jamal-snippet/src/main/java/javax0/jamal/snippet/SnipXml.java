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

public class SnipXml extends AbstractXmlDefine {
    @Override
    public String getId() {
        return "snip:xml";
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        return evaluate(input,processor,
                (String id) -> new XmlDocument(convertGlobal(id),input));
    }
}
