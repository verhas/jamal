package javax0.jamal.snake;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Closer;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;

public class Output implements Macro, InnerScopeDependent, Closer.OutputAware, Closer.ProcessorAware, AutoCloseable {
    final Yaml yaml = new Yaml();

    private Processor processor;
    private Input output;
    private String id;

    @Override
    public void close() throws Exception {
        final var yamlObject = Resolve.getYaml(processor, id);
        Resolver.resolve(yamlObject, processor, false);
        final var out = new StringWriter();
        yaml.dump(yamlObject.getContent(), out);
        output.reset();
        output.getSB().append(out);
    }

    @Override
    public void set(Processor processor) {
        this.processor = processor;
    }

    @Override
    public void set(Input output) {
        this.output = output;
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        this.id = in.toString();
        processor.deferredClose(this);
        return "";
    }

    @Override
    public String getId() {
        return "yaml:output";
    }
}
