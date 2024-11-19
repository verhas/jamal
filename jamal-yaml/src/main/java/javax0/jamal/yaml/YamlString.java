package javax0.jamal.yaml;

import javax0.jamal.api.*;

public class YamlString implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final String id = Define.getMacroIdentifier(in, processor);
        final var yamlObject = new YamlObject(processor, id, in.toString());
        processor.define(yamlObject);
        processor.getRegister().export(yamlObject.getId());
        return "";
    }

    @Override
    public String getId() {
        return "yaml:string";
    }
}
