package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Export implements Macro {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        var params = Arrays.stream(input.toString()
                .split(","))
                .map(String::trim).collect(Collectors.toList());
        for (final var param : params) {
            processor.getRegister().export(param);
        }
        return "";
    }
}
