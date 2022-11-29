package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;


public class Format {

    public static BadSyntax.ThrowingSupplier<String> msg(final String format, Object... parameters){
        return () -> String.format(format,parameters);
    }
}
