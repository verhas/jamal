package javax0.jamal.test.examples;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

// snippet Hello
public class Hello implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) {
        return "Hello, " +in.toString().trim()+"!";
    }
}
// end snippet