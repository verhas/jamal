package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.api.SpecialCharacters.DEFINE_OPTIONALLY;
import static javax0.jamal.api.SpecialCharacters.ERROR_REDEFINE;
import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces2EOL;

public class YamlString implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        skipWhiteSpaces(in);
        var optional = firstCharIs(in, DEFINE_OPTIONALLY);
        var noRedefine = firstCharIs(in, ERROR_REDEFINE);
        if (optional || noRedefine) {
            skip(in, 1);
            skipWhiteSpaces(in);
        }

        final var id = fetchId(in);
        if (processor.isDefined(convertGlobal(id))) {
            if (optional) {
                return "";
            }
            if (noRedefine) {
                throw new BadSyntax("The macro '" + id + "' was already defined.");
            }
        }
        skipWhiteSpaces(in);
        if (!firstCharIs(in, '=')) {
            throw new BadSyntax("yaml '" + id + "' has no '=' to body");
        }
        skip(in, 1);
        skipWhiteSpaces2EOL(in);
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
