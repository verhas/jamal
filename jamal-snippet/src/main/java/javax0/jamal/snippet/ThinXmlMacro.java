package javax0.jamal.snippet;

import javax0.jamal.api.*;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces2EOL;


public class ThinXmlMacro implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        skipWhiteSpaces2EOL(in);
        return new ThinXml(in.toString()).getXml();
    }

    @Override
    public String getId() {
        return "thinXml";
    }
}
