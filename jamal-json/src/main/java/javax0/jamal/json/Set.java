package javax0.jamal.json;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import org.json.JSONException;
import org.json.JSONPointer;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.InputHandler.startsWith;
import static javax0.jamal.tools.Params.holder;


public class Set implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var to = holder("jsonDataSource", "from").orElseNull().asString();
        Params.using(processor).from(this).keys(to).between("()").parse(in);

        skipWhiteSpaces(in);
        final var id = fetchId(in);
        BadSyntax.when(in.length() == 0 || in.charAt(0) != '=', "There is no '=' after the identifier in '%s'", getId());
        skip(in, 1);
        skipWhiteSpaces(in);

        final String fromId = getFromId(in, to, this);

        try {
            final var jsonMacroObject = Get.getJson(processor, fromId);
            final var newObject = new JsonMacroObject(id, getPointer(in).queryFrom(jsonMacroObject.getObject()));
            processor.getRegister().define(newObject);
            processor.getRegister().export(id);
            return "";
        } catch (JSONException | IllegalArgumentException e) {
            throw new BadSyntax("Syntax error in the expression '" + in + "'", e);
        }
    }

    static JSONPointer getPointer(String in) throws BadSyntax {
        try {
            if (in.startsWith("/"))
                return new JSONPointer(in);
            else {
                return new JSONPointer("/" + in);
            }
        } catch (JSONException | IllegalArgumentException e) {
            throw new BadSyntax("Syntax error in the JSONPointer expression '" + in + "'", e);
        }

    }

    static JSONPointer getPointer(Input in) throws BadSyntax {
        skipWhiteSpaces(in);
        return getPointer(in.toString());
    }

    static String getFromId(Input in, Params.Param<String> from, Macro me) throws BadSyntax {
        final String fromId;
        if (!from.isPresent() || from.get() == null) {
            fromId = fetchId(in);
            BadSyntax.when(startsWith(in, ".") == -1, "There is no '.' after the identifier in '%s'", fromId);
            skip(in, 1);
        } else {
            fromId = from.get();
        }
        return fromId;
    }

    @Override
    public String getId() {
        return "json:set";
    }
}
