package javax0.jamal.engine.macro;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a parameter segment in a chain of segments.
 * <p>
 * A {@code ParameterSegment} is a specialized segment that refers to a parameter
 * name, allowing its value to be dynamically retrieved from a map of parameter
 * values during macro evaluation.
 */
public class ParameterSegment extends Segment {
    /**
     * Constructs a new {@code ParameterSegment}.
     *
     * @param nextSeg the next segment in the chain.
     * @param text    the name of the parameter represented by this segment.
     */
    public ParameterSegment(Segment nextSeg, String text) {
        this.nextSeg = nextSeg;
        this.text = text;
    }

    /**
     * Retrieve the content of this segment by looking up the parameter's value.
     *
     * @param values a map of parameter names to their corresponding values.
     * @return the value of the parameter, or {@code null} if not found.
     */
    @Override
    public String content(final Map<String, String> values) {
        Objects.requireNonNull(values);
        return values.get(text);
    }

    /**
     * Splitting is not supported for {@code ParameterSegment}.
     *
     * @param parameter the parameter used to split (ignored).
     */
    public void split(String parameter) {
        // NOT SPLITTABLE
    }
}