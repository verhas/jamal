package javax0.jamal.builtins;

import javax0.jamal.api.*;
import javax0.jamal.api.Macro;
import javax0.jamal.tools.MinimumAffinityDebuggerSelector;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.ProxyDebugger;
import javax0.jamal.tools.Scan;

import java.util.List;

/**
 * This macro sets and switches on and off the debugger.
 * It only works if a debugger is available and no debugger is configured globally.
 */
public class Debug implements Macro, OptionsControlled.Core {
    final static List<Debugger> DEBUGGERS = Debugger.getInstances();

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final Params.Param<Boolean> on = Params.<Boolean>holder(null, "on").asBoolean();
        final Params.Param<Boolean> off = Params.<Boolean>holder(null, "off").asBoolean();
        final Params.Param<Boolean> noDebug = Params.<Boolean>holder("noDebug").asBoolean();
        final Params.Param<Boolean> lenient = Params.<Boolean>holder("lenient").asBoolean();
        final Params.Param<String> selector = Params.<String>holder(null, "using", "debugger", "selector").asString();
        Scan.using(processor).from(this).tillEnd().keys(on, off, selector).parse(in);

        BadSyntax.when(on.is() && off.is(), "The 'on' and 'off' parameters cannot be used together.");

        final var proxyOpt = processor.getDebugger().filter(p -> p instanceof ProxyDebugger);
        final ProxyDebugger proxy;
        if (proxyOpt.isPresent()) {
            proxy = (ProxyDebugger) proxyOpt.get();
        } else if (!lenient.is()) {
            throw new BadSyntax("The debugger is configured globally and cannot be changed.");
        } else {
            return "";
        }
        if (selector.isPresent()) {
            final var debugger = MinimumAffinityDebuggerSelector.select(DEBUGGERS, selector.get());
            try {
                if (processor.getDebuggerStub().isPresent()) {
                    debugger.init(processor.getDebuggerStub().get());
                } else {
                    throw new BadSyntax("This processor does not support debugging");
                }
            } catch (Exception e) {
                throw new BadSyntax("There was an error calling the debugger initializer", e);
            }
            proxy.setDebugger(debugger);
            return "";
        }
        if (on.is() && !noDebug.is()) {
            proxy.on();

        }
        if (off.is()) {
            proxy.off();
        }
        return "";
    }
}
