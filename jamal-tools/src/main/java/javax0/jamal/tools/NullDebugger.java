package javax0.jamal.tools;

import javax0.jamal.api.Debugger;

/**
 * A sample implementation of the {@link Debugger} interface that does nothing.
 */
public class NullDebugger implements Debugger {
    @Override
    public void setBefore(int level, CharSequence input) {

    }

    @Override
    public void setStart(CharSequence macro) {

    }

    @Override
    public void setAfter(int level, CharSequence output) {

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
