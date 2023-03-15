package javax0.jamal.json;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.UserDefinedMacro;

public class JsonMacroObject implements UserDefinedMacro, ObjectHolder<Object> {

    private final Object content;
    private final String id;

    @Override
    public Object getObject() {
        return content;
    }

    @Override
    public boolean isVerbatim() {
        return true;
    }

    public JsonMacroObject(String id, Object content) {
        this.content = content;
        this.id = id;
    }

    @Override
    public String evaluate(String... parameters) throws BadSyntax {
        BadSyntax.when(parameters.length > 1, "JSON object macro can have one argument, a JSONPointer");
        if (parameters.length > 0 && parameters[0].trim().length() > 0) {
            final var expression = JsonTools.getPointer(parameters[0].trim());
            return String.valueOf(expression.queryFrom(content));
        }
        return content.toString();
    }

    @Override
    public int expectedNumberOfArguments() {
        return 0;
    }

    @Override
    public String getId() {
        return id;
    }
}
