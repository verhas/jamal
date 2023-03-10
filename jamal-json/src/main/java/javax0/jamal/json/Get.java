package javax0.jamal.json;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import org.json.JSONException;
import org.json.JSONPointer;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.Params.holder;


public class Get implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var from = holder("jsonDataSource", "from").asString();
        Params.using(processor).from(this).keys(from).between("()").parse(in);
        final var fromId = Set.getFromId(in, from, this);
        skipWhiteSpaces(in);
        try {
            final var expression = Set.getPointer(in);
            final var jsonMacroObject = getJson(processor, fromId);
            return String.valueOf(expression.queryFrom(jsonMacroObject.getObject()));
        } catch (JSONException | IllegalArgumentException e) {
            throw new BadSyntax("Syntax error in the JSONPointer expression '" + in + "'", e);
        }
    }

    static JsonMacroObject getJson(Processor processor, String id) throws BadSyntax {
        final var identified = processor.getRegister().getUserDefined(id).orElseThrow(
                () -> new BadSyntax("Cannot resolve json '" + id + "', does not exists"));
        BadSyntax.when(!(identified instanceof JsonMacroObject), "The user defined macro '%s' is not a JSON structure", id);
        return (JsonMacroObject) identified;
    }

    @Override
    public String getId() {
        return "json:get";
    }
}
