package javax0.jamal.engine.macro;

public abstract class Segment {
    Segment nextSeg;
    String text;

    public Segment next() {
        return nextSeg;
    }

    public String content() {
        return text;
    }

    abstract public void split(String parameter, String value);
}
