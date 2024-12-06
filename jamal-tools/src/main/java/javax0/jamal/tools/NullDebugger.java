package javax0.jamal.tools;

import javax0.jamal.api.Debugger;

/**
 * A sample implementation of the {@link Debugger} interface that does nothing.
 * <p>
 * Note that this implementation is used when there is no other debugger configured, and it is NOT provided as a
 * service via the service loader mechanism. If you do so it will throw an exception during the service loading.
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
        throw new RuntimeException("This debugger must not be included in the META-INF list or as a service.");
    }

    @Override
    public void init(Stub stub) {

    }
}
