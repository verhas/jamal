package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.Processor;
import javax0.jamal.api.UserDefinedMacro;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;

public class YamlObject implements UserDefinedMacro, ObjectHolder<Object> {

    private Object content;
    final private String id;
    boolean resolved = false;
    final private Processor processor;

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

    public YamlObject(Processor processor, String id, Object content) {
        this.processor = processor;
        this.content = content;
        this.id = id;
    }

    @Override
    public String evaluate(String... parameters) throws BadSyntax {
        final var out = new StringWriter();
        final var macro = YamlDumperOptions.get(processor);
        final Yaml yaml;
        if (macro == null) {
            yaml = YamlFactory.newYaml();
        } else {
            final var options = macro.getObject();
            yaml = new Yaml(options);
        }
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
