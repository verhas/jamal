package javax0.jamal.engine.debugger;

import javax0.jamal.engine.Processor;

import java.io.IOException;

public class DebuggerFactory {

    public static Debugger build(Processor processor){
        try {
            return new ServerTcpDebugger(processor, 8080);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot bind the debugger on the port 8080",e);
        }
    }
}
