package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.OptionsMacro;

import java.util.Arrays;

public class Options implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) {
        final var options = Arrays.stream(in.toString().split("\\|", -1))
                .map(String::trim)
                .toArray(String[]::new);
        final var existingMacro = processor.getRegister().getUserMacro(OptionsMacro.OPTIONS_MACRO_ID);
        final var newMacro = new OptionsMacro();
        existingMacro.ifPresent(userDefinedMacro -> newMacro.addOptions(((OptionsMacro) userDefinedMacro).getOptions()));
        newMacro.addOptions(options);
        processor.define(newMacro);
        return "";
    }
}
