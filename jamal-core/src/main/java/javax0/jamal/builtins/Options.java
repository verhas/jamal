package javax0.jamal.builtins;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.api.SpecialCharacters;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Option;

import java.util.Arrays;

public class Options implements Macro {
    private static boolean test(String s) {
        return !s.isEmpty();
    }

    @Override
    public String evaluate(Input in, Processor processor) {
        Arrays.stream(in.toString().split("\\|", -1))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .forEach(optSetReset -> {
                final boolean value = optSetReset.charAt(0) != SpecialCharacters.OPTION_NEGATE;
                final String name = value ? optSetReset : optSetReset.substring(1);
                final var m = processor.getRegister()
                    .getUserDefined(name)
                    .filter(mac -> mac instanceof Option)
                    .map(Option.class::cast)
                    .orElseGet(() -> {
                        final var isGlobal = name.length() > 0 && name.charAt(0) == ':';
                        final var local = InputHandler.convertGlobal(optSetReset);
                        final var option = new Option(local);
                        if (isGlobal) {
                            processor.defineGlobal(option);
                        } else {
                            processor.define(option);
                        }
                        return option;
                    });
                m.set(value);
            });
        return "";
    }
}
