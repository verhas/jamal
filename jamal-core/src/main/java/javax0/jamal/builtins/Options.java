package javax0.jamal.builtins;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.OptionsStore;

public class Options implements Macro {
    private static boolean test(String s) {
        return !s.isEmpty();
    }

    @Override
    public String evaluate(Input in, Processor processor) {
        final var os = OptionsStore.getInstance(processor);
        os.addOptions(in.toString().split("\\||\\s+", -1));
        return "";
    }
}
