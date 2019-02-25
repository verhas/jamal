package javax0.jamal.engine.macro;

public class TextSegment extends Segment {
    public TextSegment(Segment next, String content){
        this.nextSeg = next;
        this.text = content;
    }

    /**
     * Split up the segment along the parameter and the value.
     * <p>
     * When a text segment gets split it looks for the first occurrence of the parameter. Then it creates two new
     * segments so that the result is three (or recursively more) segments:
     *
     * <ol>
     * <li> The current text gets truncated to the part that is before the first occurrence of the parameter.
     * <li> The second segment is a parameter segment, that will contain the name of the parameter
     * <li> The last segment will contain the text of the original text segment that is after the parameter.
     * </ol>
     *
     * The catch is that before returning the last text segment is also split for the same parameter. That way the
     * segment originally being
     *
     * <pre>
     *     bla bla ... P ... bla bla .... P ... bla bla... P ... bla
     * </pre>
     *
     * will become
     *
     * <pre>
     *     TEXT bla bla ...
     *     PARAM P
     *     TEXT ... bla bla ....
     *     PARAM P
     *     TEXT  ...bla bla...
     *     PARAM P
     *     TEXT ... bla
     * </pre>
     *
     * seven segments. The segments will be chained to each other forward through the {@code nextSeg} field that is
     * returned by the {@link #next()} method. The whole splitting mechanism modifies this chain while the same time
     * iterating through it. Since the parameter segments {@link ParameterSegment} are not splittable any more this
     * ensures that if the value of a parameter contains the name of another (or the same) parameter, it will not be
     * replaced during the splitting mechanism. At the end the segments are simply joined.
     *
     * @param parameter the name of the parameter that is used to spit the segment
     */
    @Override
    public void split(String parameter) {
        var start = text.indexOf(parameter);
        if (start != -1) {
            final var textSeg = new TextSegment(nextSeg, text.substring(start + parameter.length()));
            textSeg.split(parameter);
            final var parSeg = new ParameterSegment();
            parSeg.text = parameter;
            parSeg.nextSeg = textSeg;
            text = text.substring(0, start);
            nextSeg = parSeg;
        }
    }
}