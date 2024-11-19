package javax0.jamal.json;

import javax0.jamal.api.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Set implements Macro, InnerScopeDependent {

    /* snippet Set_macro_documentation
Add some value to an already existing JSON structure.
The format of the macro is:

{%sample/
{@json:set X/path/c=value}
%}

Here

* `X` is the name of the JSON structure that is defined in the macro registry.
  In other words, `X` is a macro defined using the macro {%ref define%}.

* `path` is the path to the value that is added to the JSON structure, names of the keys along the paths `/`
separated.
If the path is empty, then the value is added to the root of the JSON structure.

* `c` is the key of the value that is added to the JSON structure.
If this value is numeric, then the value is added to the array at the given index.
If this value is `*` then the value is added to the array at the end.

The value can be a JSON structure, a string, a number or a boolean.

==== Examples

===== Adding a value to the top level Map

This example adds a new value to the root of the JSON structure.

{%sample/
{@json:define a={a: "this is a simple JSON with a top level Map"}}
{@json:set a/b=
"this is the value to be added to json structure a"}
{a}
%}

will result:

{%output%}

===== Adding element to a Map in the JSON structure

In this example, the value is added to the value of the map from the top level named `b`.

{%sample/
{@json:define a={"a": "this is a simple JSON with a top level Map","b":{}}}
{@json:set a/b/c="this is the value to be added to json structure a"}
{a}
%}

will result:

{%output%}

===== Adding elements to an array

This example adds one element to an array.
The added element itself is an array.

{%sample/
{@json:define a=["this is a simple JSON with a top level Map","kukuruc"]}
{@json:set a/*="this is one element"}
{@json:set a/*="this is the second element"}
{a}
%}

will result:

{%output%}

    end snippet
    */
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var eq = in.indexOf("=");
        BadSyntax.when(eq == -1, "The macro 'json:set' expects a '=' sign in the parameter");
        final JsonTools tools = new JsonTools(processor);
        final var mkp = tools.getMacroPathKey(in.substring(0, eq).trim());
        final var content = tools.parseJson(in.substring(eq + 1).trim());
        if (mkp.json == null) {
            if (mkp.key == null) {
                final var jsonMacroObject = new JsonMacroObject(mkp.macroId, content);
                processor.define(jsonMacroObject);
                processor.getRegister().export(jsonMacroObject.getId());
            } else {
                throw new BadSyntax("The macro '" + mkp.macroId + "' is not a JSON macro or even does not exist.");
            }
        } else {
            final var json = tools.getJsonFromPathOrBadSyntax(mkp);
            if (json instanceof JSONArray) {
                BadSyntax.when(!mkp.key.equals("*"), String.format("The JSON structure in '%s' is an array, but the key is not '*'", getId()));
                ((JSONArray) json).put(content);
            } else if (json instanceof JSONObject) {
                ((JSONObject) json).put(mkp.key, content);
            } else {
                throw new BadSyntax("The path '" + mkp.path + "' does not point to a JSON object or array.");
            }
        }
        return "";
    }


    @Override
    public String getId() {
        return "json:set";
    }
}
