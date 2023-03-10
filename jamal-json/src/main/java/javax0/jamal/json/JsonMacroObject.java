package javax0.jamal.json;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.Processor;
import javax0.jamal.api.UserDefinedMacro;
import org.json.JSONObject;

public class JsonMacroObject implements UserDefinedMacro, ObjectHolder<Object> {

    private Object content;
    final private String id;

    @Override
    public Object getObject() {
        return content;
    }

    @Override
    public boolean isVerbatim() {
        return true;
    }

    public void setContent(JSONObject content) {
        this.content = content;
    }

    public JsonMacroObject(String id, Object content) {
        this.content = content;
        this.id = id;
    }

    @Override
    public String evaluate(String... parameters) throws BadSyntax {
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
