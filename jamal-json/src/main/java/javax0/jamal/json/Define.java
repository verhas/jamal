package javax0.jamal.json;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

import static javax0.jamal.api.SpecialCharacters.DEFINE_OPTIONALLY;
import static javax0.jamal.api.SpecialCharacters.ERROR_REDEFINE;
import static javax0.jamal.tools.InputHandler.*;

@Macro.Name("json:define")
public class Define implements Macro, InnerScopeDependent, Scanner {
/* snippet Define_macro_documentation
You can use this macro to define a JSon structure.
A JSON structure can be a map, a list or a string.
JSon supports other primitive values, but Jamal being a text macro processor handles all other primitive types as strings.

The format of the macro is

{%sample/
{@json:define jsonMacro=JSON content}
%}

After the execution of this macro, the name `jsonMacro` will be defined as a user-defined macro and can be used as `pass:[{jsonMacro}]`.
The value will replace the place of the use with the actual unformatted JSON content.

NOTE: Internally, Jamal converts the JSON read in an object structure consisting of strings, primitives, maps, and lists.
The structure is stored in an object of the type {%@java:class (format=`$simpleName`) javax0.jamal.json.JsonMacroObject%}.
This class technically is a user-defined macro.
The `json:define` macro will register the structure among the user-defined macros.
When the name is used the same way as any other user-defined macro (without any argument), the content of the JSON structure is converted to text.

The `jsonMacro` is stored along with the "usual" user-defined macros.
Any usual or other user-defined macro can be redefined any number of times.
If you want to define a JSON macro only if it was not defined prior, use the `?` after the keyword `json:define`.
If you want to get an error message if the macro was already defined, use the `!` after the keyword `json:define`.
This functionality is implemented the same way as it is for the core built-in macro `define`.

The core `define` macro also has options to drive these behaviour.
The `json:define` macro does not.

The example:

{%sample/
{@json:define xyz={
a: this is the string value of a,
b:[ first value of b,second value in b],
c: {a: this is c.a,b: this is c.b}}
}\
{xyz}
%}

will result

{%output%}

The advantage of using this macro over just writing the JSON directly to the output is that:

* You can use user-defined macro parameters mixing the json content with Jamal macros.

* You can modify the structure using the `json:set` macro.

Utilizing user-defined macros, you can use macros inside JSON code, and at the same time, you can use JSON code inside the macros.
That way, you can pull out the part, repeat, and use only the macro as a reference.

[NOTE]
====
When processing JSON input, you can use the `{` and `}` characters as macro opening and macro closing strings.
These characters are paired in the JSON input, therefore they will not interfere with the macro processing.
That is only if we assume that the JSON containing macros do not contain macros themselves, and they are invoked using the `@` in front of their name.
However, when there is a need to evaluate macros before interpreting the JSON, the `{` and `}` characters may cause problems.
You can overcome this setting the macro opening and closing stings to something else, like `{%%}` and `%}`.
You can also modify the JSON using `{}` in place of every `{` and a `}` in place of every `}`.
This will disturb the balance of the `{` and `}` characters, that may hinder some editor navigation.
You can also use the `{@ident...}` to protect the parts that are pure JSON content without macros.

The recommended way is to use something different from `{`and `}` as macro opening and closing strings.
====
end snippet
     */

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var line = scanner.bool(null, "line", "JSONL", "jsonl");
        scanner.done();

        final String id = getMacroIdentifier(in, processor);
        if (id == null) return "";
        final var tools = new JsonTools(processor);
        final Object jsonStructure;
        try {
            if (line.is()) {
                jsonStructure = tools.parseJsonLines(in);
            } else {
                jsonStructure = tools.parseJson(in);
            }
        } catch (Exception e) {
            throw new BadSyntax("Cannot load JSON data.", e);
        }
        final var jsonMacroObject = new JsonMacroObject(id, jsonStructure);
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

}
