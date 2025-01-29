package javax0.jamal.tools;

import javax0.jamal.api.Debugger;

/**
 * A sample implementation of the {@link Debugger} interface that calls another debugger.
 * By default, the debugger calls the null debugger.
 * The used debugger can be set any time calling the {@link #setDebugger(Debugger) setDebugger} method.
 * It is also possible to switch off debugging calling the {@link #off()} method.
 * When a debugger was already set and the debugging was switched off you can switch it on calling the {@link #on()}
 * method.
 */
public class ProxyDebugger implements Debugger {
    private static final Debugger nullDebugger = new NullDebugger();

    private Debugger debugger = nullDebugger;
    private Debugger on = nullDebugger;
    private final Debugger off = nullDebugger;

    /**
     * Set the debugger that will be called. If the debugger is null then the null debugger is used.
     * @param debugger the debugger to be called.
     */
    public void setDebugger(Debugger debugger) {
        if( debugger == null ) {
            this.debugger = nullDebugger;
            this.on = nullDebugger;
        }else{
            this.debugger = debugger;
            this.on = debugger;
        }
    }

    /**
     * Switch on the debugging.
     */
    public void on() {
        debugger = on;
    }

    /**
     * Switch off the debugging.
     */
    public void off() {
        debugger = off;
    }

    @Override
    public void setBefore(int level, CharSequence input) {
        debugger.setBefore(level, input);
    }

    @Override
    public void setStart(CharSequence macro) {
        debugger.setStart(macro);
    }


    @Override
    public void setAfter(int level, CharSequence output) {
        debugger.setAfter(level, output);
    }

    @Override
    public void close() {
        debugger.close();
    }

    @Override
    public int affinity(String s) {
        return debugger.affinity(s);
    }

    @Override
    public void init(Stub stub) throws Exception {
        debugger.init(stub);
    }
}
