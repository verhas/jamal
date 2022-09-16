package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Processor;

import static javax0.jamal.tools.InputHandler.convertGlobal;

public class Xml extends AbstractXmlDefine {
    @Override
    public String getId() {
        return "xml:define";
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        return evaluate(input,processor,
                (id) -> new XmlDocument(convertGlobal(id),input.toString()));
    }
}
