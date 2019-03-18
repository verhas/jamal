package javax0.jamal.builtins;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.OptionsStore;

import java.util.Arrays;

public class Options implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) {
        final var options = Arrays.stream(in.toString().split("\\|", -1))
                .map(String::trim)
                .toArray(String[]::new);
        final var existing = processor.getRegister().getUserDefined(OptionsStore.OPTIONS_MACRO_ID);
        final var newUserDefined = new OptionsStore();
        existing.ifPresent(userDefinedMacro -> newUserDefined.addOptions(((OptionsStore) userDefinedMacro).getOptions()));
        newUserDefined.addOptions(options);
        processor.define(newUserDefined);
        return "";
    }
}
