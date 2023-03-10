package javax0.jamal.json;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointer;

import static javax0.jamal.tools.Params.holder;

public class Add implements Macro, InnerScopeDependent {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var to = holder("jsonDataTarget", "to").asString();
        final var key = Params.<String>holder(null, "key").orElseNull();
        final var flatten = holder(null, "flat", "flatten").asBoolean();
        Params.using(processor).from(this).keys(to, key, flatten).parse(in);
        final var dotIndex = to.get().indexOf('.');
        final String id = getId(to, dotIndex);
        final String expression = getJsonExpression(to, dotIndex);
        final Object jsonStructure = parseJson(in);
        final var jsonMacroObject = Get.getJson(processor, id);
        final Object anchor = getAnchor(expression, to, jsonMacroObject);
        assertConsistency(to, key, flatten, anchor);
        if (anchor instanceof JSONObject) {
            if (flatten.is()) {
                BadSyntax.when(!(jsonStructure instanceof JSONObject), "You can add only a Map to a Map when flat(ten) for '%s'", to.get());
                ((JSONObject) jsonStructure).keySet().forEach(k -> ((JSONObject) anchor).put(k, ((JSONObject) jsonStructure).get(k)));
            } else {
                final var anchorJSONObject = (JSONObject) anchor;
                anchorJSONObject.put(key.get(), jsonStructure);
            }
        } else {
            if (flatten.is()) {
                BadSyntax.when(!(jsonStructure instanceof JSONArray) || !(anchor instanceof JSONArray), "You can add only a List to a List when flat(tten) for '%s'", to.get());
                final var anchorJSONArray = (JSONArray) anchor;
                final var jsonStructureJSONArray = (JSONArray) jsonStructure;
                for (int i = 0; i < jsonStructureJSONArray.length(); i++) {
                    anchorJSONArray.put(jsonStructureJSONArray.get(i));
                }
            } else {
                ((JSONArray) anchor).put(jsonStructure);
            }
        }
        return "";
    }

    private void assertConsistency(Params.Param<String> to, Params.Param<String> key, Params.Param<Boolean> flatten, Object anchor) throws BadSyntax {
        BadSyntax.when(key.get() == null && anchor instanceof JSONObject && !flatten.is(), "You cannot '%s' without a 'key' parameter to a Map for '%s'", getId(), to.get());
        BadSyntax.when(key.get() != null && anchor instanceof JSONArray, "You cannot '%s' with a 'key' parameter to a List for '%s'", getId(), to.get());
        BadSyntax.when(key.get() != null && flatten.is(), "You cannot '%s' with a 'key' parameter when flattening for '%s'", getId(), to.get());
        BadSyntax.when((!(anchor instanceof JSONObject)) && !(anchor instanceof JSONArray), "You can '%s' only to a List or Map for '%s'\nThe actual class is %s", getId(), to.get(), anchor.getClass());
    }

    private Object getAnchor(String expression, Params.Param<String> to, JsonMacroObject jsonMacroObject) throws BadSyntax {
        Object anchor;
        Exception exception = null;
        try {
            if (expression == null) {
                anchor = jsonMacroObject.getObject();
            } else {
                anchor = Set.getPointer(expression).queryFrom(jsonMacroObject.getObject());
            }
        } catch (JSONException | IllegalArgumentException e) {
            anchor = null;
            exception = e;
        }
        if (anchor == null) {
            throw new BadSyntax("Cannot '" + getId() + "' into the JSON expression '" + to.get() + "'", exception);
        }
        return anchor;
    }

    static Object parseJson(Input in) throws BadSyntax {
        final var s = in.toString().trim();
        try {
            if (s.charAt(0) == '{') {
                return new JSONObject(in.toString());
            }
            if (s.charAt(0) == '[') {
                return new JSONArray(in.toString());
            }
            if (s.charAt(0) == '"' || s.charAt(0) == '\'') {
                return s.substring(1, s.length() - 1);
            }
            return in.toString();
        } catch (Exception e) {
            throw new BadSyntax("Cannot load JSON data.", e);
        }
    }

    private String getJsonExpression(Params.Param<String> to, int dotIndex) throws BadSyntax {
        return dotIndex == -1 ? null : to.get().substring(dotIndex + 1);
    }

    private String getId(Params.Param<String> to, int dotIndex) throws BadSyntax {
        return dotIndex == -1 ? to.get() : to.get().substring(0, dotIndex);
    }

    @Override
    public String getId() {
        return "json:add";
    }
}
