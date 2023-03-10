package javax0.jamal.json;

import javax0.jamal.api.Closer;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

@Macro.Stateful
public class Output implements Macro, InnerScopeDependent, Closer.OutputAware, Closer.ProcessorAware, AutoCloseable {

    private Processor processor;
    private Input output;
    private String id;

    @Override
    public void close() throws Exception {
        output.getSB().append(Get.getJson(processor, id).getObject().toString());
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
    public String evaluate(Input in, Processor processor) {
        InputHandler.skipWhiteSpaces(in);
        this.id = in.toString();
        processor.deferredClose(this);
        return "";
    }

    @Override
    public String getId() {
        return "json:output";
    }
}
