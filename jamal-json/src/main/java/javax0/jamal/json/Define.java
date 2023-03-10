package javax0.jamal.json;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import org.json.JSONObject;

import static javax0.jamal.api.SpecialCharacters.DEFINE_OPTIONALLY;
import static javax0.jamal.api.SpecialCharacters.ERROR_REDEFINE;
import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces2EOL;

public class Define implements Macro, InnerScopeDependent {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final String id = getMacroIdentifier(in, processor);
        if (id == null) return "";
        final Object jsonStructure;
        try {
            jsonStructure = Add.parseJson(in);
        } catch (Exception e) {
            throw new BadSyntax("Cannot load JSON data.", e);
        }
        final var jsonMacroObject = new JsonMacroObject( id, jsonStructure);
        processor.define(jsonMacroObject);
        processor.getRegister().export(jsonMacroObject.getId());
        return "";
    }

    static String getMacroIdentifier(Input in, Processor processor) throws BadSyntax {
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
                return null;
            }
            BadSyntax.when(noRedefine, "The macro '%s' was already defined.", id);
        }
        skipWhiteSpaces(in);
        BadSyntax.when(!firstCharIs(in, '='), "json '%s' has no '=' to body", id);
        skip(in, 1);
        skipWhiteSpaces2EOL(in);
        return id;
    }

    @Override
    public String getId() {
        return "json:define";
    }
}
