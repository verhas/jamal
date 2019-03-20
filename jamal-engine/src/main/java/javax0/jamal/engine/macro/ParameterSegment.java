package javax0.jamal.engine.macro;

public class ParameterSegment extends Segment {
    public ParameterSegment(Segment nextSeg, String text) {
        this.nextSeg = nextSeg;
        this.text = text;
    }

    @Override
    public void split(String parameter) {
        // NOT SPLITTABLE
    }
}