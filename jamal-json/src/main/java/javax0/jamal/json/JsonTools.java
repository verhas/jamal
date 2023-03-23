package javax0.jamal.json;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Processor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointer;

import java.util.Objects;

class JsonTools {

    private final Processor processor;

    JsonTools(final Processor processor) {
        this.processor = processor;
    }


    JSONArray parseJsonLines(Input in) throws BadSyntax {
        final var array = new JSONArray();
        final var lines = in.toString().split("\n");
        for (var line : lines) {
            array.put(parseJson(line));
        }
        return array;
    }

    Object parseJson(Input in) throws BadSyntax {
        return parseJson(in.toString().trim());
    }

    Object parseJson(String s) throws BadSyntax {
        try {
            if (s.charAt(0) == '{') {
                return new JSONObject(s);
            }
            if (s.charAt(0) == '[') {
                return new JSONArray(s);
            }
            if (s.charAt(0) == '"' || s.charAt(0) == '\'') {
                return s.substring(1, s.length() - 1);
            }
            return s;
        } catch (Exception e) {
            throw new BadSyntax("Cannot load JSON data.", e);
        }
    }

    /**
     * The parsed result of a {@code macro_name '/' path '/' key} expression.
     * <p>
     * The 'macro_name' is always present. It will be stored in the {@link #macroId} field. It is always filled.
     * <p>
     * When the input has the format {@code macro_name '/' key} then the {@link #path} field will be  {@code null},
     * and fields {@link #key}, {@link #json}, and {@link #macroId} will be filled.
     * <p>
     * If there is no defined macro with the given macroId, or there is, but it is not a {@link JsonMacroObject} then
     * the field {@link #json} will be {@code null}.
     */
    static class MacroPathKey {
        final JSONPointer path;
        final String key;
        final Object json;
        final String macroId;


        MacroPathKey(String path, String key, final Object json, final String macroId) throws BadSyntax {
            try {
                this.path = path == null ? null : new JSONPointer(path);
            } catch (IllegalArgumentException e) {
                throw new BadSyntax(String.format("The anchor '%s' is not a valid JSON pointer", path), e);
            }
            this.key = key;
            this.json = json;
            this.macroId = Objects.requireNonNull(macroId, "The macroId cannot be null");
        }
    }

    /**
     * Parse the input string that has to have the format
     * <pre>
     *     {@code
     *     macro_name '/' path '/' key
     *     }
     * </pre>
     * <p>
     * and return the triplet.
     *
     * @param s the expression pointing to the macro, path, and the key
     * @return the structure
     */
    MacroPathKey getMacroPathKey(final String s) throws BadSyntax {
        final var start = s.indexOf('/');
        final var lastIndex = s.lastIndexOf('/');
        if (start == -1) {
            /*
             * There is no '/' in the string. This means that the string is the name of the macro and a new macro is to be defined
             */
            return new MacroPathKey(null, null, null, s);
        } else {
            if (lastIndex == start) {
                /*
                 * There is only one '/' in the string. This means that the string is the name of the macro and a top level key
                 */
                return new MacroPathKey(null, s.substring(start + 1), getJson(s.substring(0, start)).getObject(), s.substring(0, start));
            } else {
                return new MacroPathKey(s.substring(start, lastIndex), s.substring(lastIndex + 1), getJson(s.substring(0, start)).getObject(), s.substring(0, start));
            }
        }
    }

    MacroPathKey getMacroPath(final String s) throws BadSyntax {
        final var slash = s.indexOf('/');
        if (slash == -1) {
            /*
             * There is no '/' in the string. This means that the string is the name of the macro
             */
            return new MacroPathKey(null, null, getJson(s).getObject(), s);
        } else {
            return new MacroPathKey(s.substring(slash), null, getJson(s.substring(0, slash)).getObject(), s.substring(0, slash));
        }
    }

    Object getJsonFromPath(final JsonTools.MacroPathKey mkp) {
        return mkp.path == null ? mkp.json : mkp.path.queryFrom(mkp.json);
    }

    Object getJsonFromPathOrBadSyntax(final JsonTools.MacroPathKey mkp) throws BadSyntax {
        try {
            return getJsonFromPath(mkp);
        } catch (Exception e) {
            throw new BadSyntax("The path '" + mkp.path + "' is not valid or cannot be evaluated for the given JSON.", e);
        }
    }

    private static final JsonMacroObject NULL = new JsonMacroObject(null, null);

    /**
     * Get a user defined JSON macro object from the macro registry belonging to this processor.
     *
     * @param id the name of the user defined macro
     * @return the found object or an object that returns NULL as object if there is no such macro
     */
    JsonMacroObject getJson(String id) {
        return (JsonMacroObject) processor.getRegister().getUserDefined(id).filter(m -> m instanceof JsonMacroObject).orElse(NULL);
    }

    static JSONPointer getPointer(String s) throws BadSyntax {
        try {
            if (s.startsWith("/")) {
                return new JSONPointer(s);
            } else {
                return new JSONPointer("/" + s);

            }
        } catch (JSONException | IllegalArgumentException e) {
            throw new BadSyntax("Syntax error in the JSONPointer expression '" + s + "'", e);
        }

    }
}
