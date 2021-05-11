package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Debuggable;
import javax0.jamal.api.Debugger;
import javax0.jamal.tools.Input;

import java.util.List;

/**
 * The {@link javax0.jamal.api.Debugger.Stub} implementation that works together with the {@code jamal.engine} {@link
 * Processor}.
 */
public class DebuggerStub implements Debugger.Stub {

    private final Processor processor;

    public DebuggerStub(Processor processor) {
        this.processor = processor;
    }

    public List<Debuggable.Scope> getScopeList() {
        return processor.getRegister().debuggable().map(Debuggable.MacroRegister::getScopes).orElse(List.of());
    }

    public String process(String in) throws BadSyntax {
        return processor.process(Input.makeInput(in));
    }
}
