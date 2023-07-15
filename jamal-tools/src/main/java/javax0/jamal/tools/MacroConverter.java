package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Processor;

import java.util.function.Function;

public class MacroConverter {


    public static Function<String[], String> toFunction(final Processor processor, final String macroName) {
        return processor.getRegister().getUserDefined(macroName)
                .filter(m -> m instanceof Evaluable)
                .map(m -> (Evaluable) m)
                .map(m -> (Function<String[], String>) args -> {
                            try {
                                return m.evaluate(args);
                            } catch (BadSyntax e) {
                                throw new RuntimeException(e);
                            }
                        }
                ).orElseThrow(() -> new IllegalArgumentException("Macro " + macroName + " is not defined"));
    }

}
