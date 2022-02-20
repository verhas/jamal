package javax0.jamal.io;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class OsName implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) {
        return System.getProperty("os.name");
    }

    @Override
    public String getId() {
        return "os:name";
    }
}
