package javax0.jamal.snake;

import javax0.jamal.api.UserDefinedMacro;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;

public class YamlObject implements UserDefinedMacro {
    final Yaml yaml = new Yaml();

    boolean resolved = false;

    public Object getContent() {
        return content;
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
