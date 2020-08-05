package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Processor;

import java.util.Optional;

public class UDMacro {
 final private String id;

    private UDMacro(String id) {
        this.id = id;
    }

    public static UDMacro macro(String id){
        return new UDMacro(id);
    }

    public Optional<String> from(Processor processor){
        return processor.getRegister().getUserDefined(id)
            .filter(macro -> macro instanceof Evaluable)
            .map(macro -> (Evaluable) macro)
            .map(macro -> {
                try {
                    return macro.evaluate();
                } catch (BadSyntax bs) {
                    return null;
                }
            });
    }

}
