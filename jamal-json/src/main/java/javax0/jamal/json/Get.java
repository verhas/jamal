package javax0.jamal.json;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import org.json.JSONException;
/* snippet Get_macro_documentation
This macro will fetch one value or a "sub" json from a JSON structure.
This can be useful when you want to document some configuration or other data structure that is present as a JSON file in your project.
In that case you can import the JSON structure into your Jamal document and refer individual values in it.
The format of the macro is:

{%sample/
{@json:get macro_name/JSONPointer}
%}

The same result can be achieved simply writing the JSONPointer after the name of the JSON macro defined using {%ref define%}.
In that case you can omit the `@json:get `  starting with the macro name.

The `JSONPointer` is navigational path documented in the link:https://stleary.github.io/JSON-java/org/json/JSONPointer.html[JavaDoc] api of the JSON library this macro package uses:

> A JSON Pointer is a simple query language defined for JSON documents by RFC 6901.
In a nutshell, JSONPointer allows the user to navigate into a JSON document using strings, and retrieve targeted objects, like a simple form of XPATH.
Path segments are separated by the '/' char, which signifies the root of the document when it appears as the first char of the string.
Array elements are navigated using ordinals, counting from 0.
JSONPointer strings may be extended to any arbitrary number of segments.

If the navigation is successful, the matched item is returned.
A matched item may be a JSONObject, a JSONArray, or a string.
If the JSONPointer string building or the navigation fails a `BadSyntax` exception will happen.

When getting a value out of a JSON user defined macro the macro will automatically be resolved.

==== Examples

{%sample/
{@json:define a={a:"alma",b:2,c: 3,d:[1,2,{q:{h:"deep h"}}]}}\
@json:get a/d/2/q/h = "{@json:get a/d/2/q/h}"
a d/2/q/h = "{a d/2/q/h}"
%}

will result

{%output%}

NOTE: The macro `json:get` is somewhat superfluous, because you can get the same result using the JSON user defined macro with the JSONPointer as parameter.
However, as you can see from the example above, the different approaches provide different readability. Choose wisely.
end snippet
*/
public class Get implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var s = in.toString().trim();
        try {
            final var tools = new JsonTools(processor);
            final var mkp = tools.getMacroPath(s);
            if (mkp.json == null) {
                throw new BadSyntax("The JSON path '" + s + "' is not defined");
            }
            final var json = tools.getJsonFromPath(mkp);
            return json.toString();
        } catch (JSONException | IllegalArgumentException e) {
            throw new BadSyntax("Syntax error in the JSONPointer expression '" + in + "'", e);
        }
    }

    @Override
    public String getId() {
        return "json:get";
    }
}
