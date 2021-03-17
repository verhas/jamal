package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Debuggable;
import javax0.jamal.api.Debugger;
import javax0.jamal.tools.Input;

import java.util.List;

public class DebuggerStub implements Debugger.Stub{

    private final Processor processor;

    public DebuggerStub(Processor processor) {
        this.processor = processor;
    }

    public List<Debuggable.Scope> getScopeList() {
        return ((Debuggable.MacroRegister) processor.getRegister()).getScopes();
    }

    public void process(String in) throws BadSyntax {
        processor.process(Input.makeInput(in));
    }
}
