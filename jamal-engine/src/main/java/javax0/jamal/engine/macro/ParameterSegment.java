package javax0.jamal.engine.macro;

import java.util.Map;
import java.util.Objects;

public class ParameterSegment extends Segment {
    public ParameterSegment(Segment nextSeg, String text) {
        this.nextSeg = nextSeg;
        this.text = text;
    }

    @Override
    public String content(final Map<String, String> values) {
        Objects.requireNonNull(values);
        return values.get(text);
    }

    public void split(String parameter) {
        // NOT SPLITTABLE
    }
}