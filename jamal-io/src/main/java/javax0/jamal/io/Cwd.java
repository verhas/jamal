package javax0.jamal.io;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.io.File;

@Macro.Name("io:cwd")
public class Cwd implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) {
        return new File(".").getAbsolutePath();
    }
}
