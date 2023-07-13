package javax0.jamal.json;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import org.json.JSONException;
import org.json.JSONObject;

/* snippet Keys_macro_documentation
This macro will fetch one value or a "sub" json from a JSON structure and returns the keys of the structure.
If the result is a boolean, string, number or JSON objects, essentially anything else than an JSON structure, then an error will happen.

The result can be used to iterate through the elements using the core macro `for`.

{%sample/
{@json:keys macro_name/JSONPointer}
%}

or

{%sample/
{@json:keys macro_name/JSONPointer | macro_name/JSONPointer | ...}
%}

The second format will try to get the first, then the second and so on pointer from one or more JSON structures until one of them is found.
If one of the pointers finds a value but that is not a structure then an error will happen.

The keys are separated by the separator character.
The default separator is a comma.
The separator can be changed by the parameter `separator` or `sep`.

==== Examples

{%sample/
{@json:define a={a:"alma",b:2,c: 3,d:[1,2,{q:{h:"deep h"}}]}}\
@json:keys a/ = "{@json:keys a}"
%}

will result

{%output%}

end snippet
*/
public class Keys implements Macro, InnerScopeDependent, Scanner {

    private static final String INVALID_PATH = "The path '%s' is not valid, is not a structure or cannot be evaluated for the given JSON.";

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var sep = scanner.str("separator", "sep").defaultValue(",");
        scanner.done();

        InputHandler.skipWhiteSpaces(in);
        final var paths = in.toString().trim().split("\\|");
        final var tools = new JsonTools(processor);
        for (final var s : paths) {
            try {
                final var mkp = tools.getMacroPath(s.trim());
                BadSyntax.when(paths.length == 1 && mkp.json == null, "There is no macro named '%s' in the registry containing a JSON object", mkp.macroId);
                if (mkp.json != null) {
                    final var json = tools.getJsonFromPath(mkp);
                    BadSyntax.when(!(json instanceof JSONObject), INVALID_PATH, in);
                    return String.join(sep.get(), ((JSONObject) json).keySet());
                }
            } catch (JSONException | IllegalArgumentException e) {
                if (paths.length == 1) {
                    throw new BadSyntax(String.format(INVALID_PATH, in), e);
                }
            }
        }
        throw new BadSyntax(String.format(INVALID_PATH, in));
    }

    @Override
    public String getId() {
        return "json:keys";
    }
}
