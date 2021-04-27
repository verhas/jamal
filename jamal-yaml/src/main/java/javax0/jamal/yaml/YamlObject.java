package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.UserDefinedMacro;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class YamlObject implements UserDefinedMacro, ObjectHolder {
    private final Yaml yaml = new Yaml();

    boolean resolved = false;

    @Override
    public Object getObject() {
        return content;
    }

    @Override
    public boolean isVerbatim() {
        return true;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    private Object content;
    final private String id;

    public YamlObject(Object content, String id) {
        this.content = content;
        this.id = id;
    }

    @Override
    public String evaluate(String... parameters) {
        final var out = new StringWriter();
        yaml.dump(content, out);
        return out.toString();
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
