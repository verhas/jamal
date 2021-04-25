package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Closer;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;

public class Output implements Macro, InnerScopeDependent, Closer.OutputAware, Closer.ProcessorAware, AutoCloseable {
    final Yaml yaml = new Yaml();

    private Processor processor;
    private Input output;
    private String id;
    private boolean clone, copy;

    @Override
    public void close() throws Exception {

        final var yamlObject = Resolve.getYaml(processor, id);
        Resolver.resolve(yamlObject, processor, clone, copy);
        final var out = new StringWriter();
        yaml.dump(yamlObject.getObject(), out);
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
        final var clone = Resolver.cloneOption();
        final var copy = Resolver.copyOption();
        Params.using(processor).keys(clone, copy).between("()").parse(in);
        this.clone = clone.is();
        this.copy = copy.is();
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
