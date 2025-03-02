package javax0.jamal.json;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import org.json.JSONArray;
import org.json.JSONException;

/* snippet Length_macro_documentation
This macro can be used to get the length of a JSON array.
The macro first fetches the JSON value using the argument the same way as `json:get` does, but instead of the value it returns the length of the array.
If the value is a boolean, string, number or JSON objects, essentially anything else than an array, then an error will happen.

The result can be used to iterate through the elements, for example using the macros of the module `jamal-prog`.

{%sample/
{@json:length macro_name/JSONPointer}
%}

or

{%sample/
{@json:length macro_name/JSONPointer | macro_name/JSONPointer | ...}
%}

The second format will try to get the first, then the second and so on pointer from one or more JSON structures until one of them is found.
If one of the pointers finds a value but that is not an array then an error will happen.

==== Examples

{%sample/
{@json:define a={a:"alma",b:2,c: 3,d:[1,2,{q:{h:"deep h"}}]}}\
@json:length a/d/ = "{@json:get a/d}"
%}

will result

{%output%}

end snippet
*/
@Macro.Name("json:length")
public
class Length implements Macro, InnerScopeDependent {

    private static final String INVALID_PATH = "The path '%s' is not valid, is not an array or cannot be evaluated for the given JSON.";

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        final var paths = in.toString().trim().split("\\|");
        final var tools = new JsonTools(processor);
        for (final var s : paths) {
            try {
                final var mkp = tools.getMacroPath(s.trim());
                BadSyntax.when(paths.length == 1 && mkp.json == null, "There is no macro named '%s' in the registry containing a JSON object", mkp.macroId);
                if (mkp.json != null) {
                    final var json = tools.getJsonFromPath(mkp);
                    BadSyntax.when(!(json instanceof JSONArray), INVALID_PATH, in);
                    return Integer.toString(((JSONArray) json).length());
                }
            } catch (JSONException | IllegalArgumentException e) {
                if (paths.length == 1) {
                    throw new BadSyntax(String.format(INVALID_PATH, in), e);
                }
            }
        }
        throw new BadSyntax(String.format(INVALID_PATH, in));
    }

}
