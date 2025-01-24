package javax0.jamal.engine.macro;

import java.util.Map;

/**
 * A segment is a string and a "pointer" to the next segment.
 * <p>
 * During macro evaluation, the actual values replace the names of the parameters in the content.
 * However, parameter names, which appear in the text only during the evaluation of the macro, must not be replaced.
 * For example
 * <pre>
 * {@code
 *    {@define A(X)=X{B}X}{@define B=X}{A/3}
 * }
 * </pre>
 *
 * should result {@code 3X3} and not {@code 333}
 *
 * <p>
 * In other words, if the actual value of a parameter contains the name of another, or the same parameter, then
 * that name must not be replaced.
 * <p>
 * To achieve this, the text of the macro is split into segments.
 * The segments contain the parameters and the texts between the parameter names.
 * <p>
 * When a macro is evaluated, the first time, the content is split into segments and then these segments are joined
 * together after replacing the parameter names to their corresponding values.
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
