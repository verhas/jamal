package javax0.jamal.engine;

import javax0.jamal.api.Debugger;

public class NullDebugger implements Debugger {
    @Override
    public void setBefore(int level, CharSequence input) {

    }

    @Override
    public void setStart(CharSequence macro) {

    }

    @Override
    public void setAfter(int level, CharSequence input, CharSequence output) {

    }

    @Override
    public void close() {

    }

    @Override
    public int affinity(String s) {
        return Integer.MAX_VALUE-1;
    }

    @Override
    public void init(Stub stub) {

    }
}
