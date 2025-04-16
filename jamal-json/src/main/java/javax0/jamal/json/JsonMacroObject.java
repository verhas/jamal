package javax0.jamal.json;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.tools.IdentifiedObjectHolder;

public class JsonMacroObject extends IdentifiedObjectHolder<Object> implements UserDefinedMacro {

    @Override
    public boolean isVerbatim() {
        return true;
    }

    public JsonMacroObject(String id, Object content) {
        super(content, id);
    }

    @Override
    public String evaluate(String... parameters) throws BadSyntax {
        BadSyntax.when(parameters.length > 1, "JSON object macro can have one argument, a JSONPointer");
        if (parameters.length > 0 && !parameters[0].trim().isEmpty()) {
            try {
                final var expression = JsonTools.getPointer(parameters[0].trim());
                return String.valueOf(expression.queryFrom(getObject()));
            } catch (final Exception e) {
                throw new BadSyntax(String.format("JSON object '%s' cannot be queried with '%s'", getId(), parameters[0]), e);
            }
        }
        return getObject().toString();
    }

    @Override
    public int expectedNumberOfArguments() {
        return 0;
    }
}
