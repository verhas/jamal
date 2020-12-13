package javax0.jamal.engine.macro;

import java.util.Map;

/**
 * A segment contains a small piece of text and segments are chained forward to be a linked list.
 * <p>
 * During macro evaluation the names of the parameters in the content are replaced by the actual values. To avoid
 * non-deterministic macro evaluation the content of a user defined macro is split into segments. The segments contain
 * the parameters and the texts between the parameter names.
 * <p>
 * When a macro is evaluated the first time it's content is split into segments and then these segments are joined
 * together so that the parameter names are replaced with the actual values.
 */
public abstract class Segment {
    Segment nextSeg;
    String text;

    public Segment next() {
        return nextSeg;
    }

    public String content(final Map<String, String> values) {
        return text;
    }

    abstract public void split(String parameter);
}
