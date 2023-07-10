package javax0.jamal.engine;

import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Processor;

/**
 * This is a special user defined macro that the processor initializes and uses when the macro opening string is
 * immediately followed by the macro closing string. In that case this user defined macro just returns the macro
 * opening string.
 */
public class NullMacro implements Identified, Evaluable {
    private final String open;

    public NullMacro(final Processor processor) {
        this.open = processor.getRegister().open();
    }

    @Override
    public String evaluate(final String... parameters) {
        return open;
    }

    /**
     * We do not want the single macro opening string to be evaluated as the result of the macro, therefore this
     * macro is verbatim.
     * @return {@code true}
     */
    @Override
    public boolean isVerbatim() {
        return true;
    }

    @Override
    public int expectedNumberOfArguments() {
        return 0;
    }

    @Override
    public String getId() {
        return "";
    }
}
