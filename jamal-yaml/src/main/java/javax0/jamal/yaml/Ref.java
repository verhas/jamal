package javax0.jamal.yaml;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

@Macro.Name("yaml:ref")
public
class Ref implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) {
        return "!ref " + in;
    }

}
