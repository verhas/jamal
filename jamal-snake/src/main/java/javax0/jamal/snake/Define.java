package javax0.jamal.snake;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import org.yaml.snakeyaml.Yaml;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces2EOL;

public class Define implements Macro, InnerScopeDependent {
    final Yaml yaml = new Yaml();

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        skipWhiteSpaces(in);
        final var id = fetchId(in);
        skipWhiteSpaces(in);
        if (!firstCharIs(in, '=')) {
            throw new BadSyntax("yaml '" + id + "' has no '=' to body");
        }
        skip(in, 1);
        skipWhiteSpaces2EOL(in);
        final var yamlStructure = yaml.load(in.toString());
        final var yamlObject = new YamlObject(yamlStructure, id);
        processor.define(yamlObject);
        processor.getRegister().export(yamlObject.getId());
        return "";
    }

    @Override
    public String getId() {
        return "yaml:define";
    }
}
