package javax0.jamal.builtins;

import javax0.jamal.api.Macro;
import javax0.jamal.api.*;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

/**
 * Block macro used to enclose some operations into a scope. Can be used similarly as {@code Begin} and {@code End},
 * but the scope is not named. The two things are equivalent, it is a readability issue which one to use.
 * <p>
 * The implementation, as easily can be seen is the same as the macro {@link Comment}.
 */
public class Block implements Macro, InnerScopeDependent {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var flat = Params.<String>holder(null, "flat","export").asBoolean();
        Scan.using(processor).from(this).between("[]").keys(flat).parse(in);
        if( flat.is()){
            processor.getRegister().export();
        }
        return "";
    }
}
