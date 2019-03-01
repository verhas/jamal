package javax0.jamal.engine.macro;

public class TextSegment extends Segment {
    public TextSegment(Segment next, String content) {
        this.nextSeg = next;
        this.text = content;
    }

    /**
     * Iterate over the segments and perform the splitting.
     *
     * @param root      the segment to split
     * @param parameter the name of the parameter that is used to spit the segment
     */
    private static void split(final TextSegment root, final String parameter) {
        var it = root;
        while ((it = splitAndGetNext(it, parameter)) != null) ;
    }

    /**
     * Split up the segment to three segment: a text segment with text before the parameter text, a parameter segment
     * and a text segment with the text after the parameter and after this is done return the last text segment.
     *
     * @param it        the text segment to split
     * @param parameter the parameter that splits the segment into two
     * @return the second text segment or null in case the segment can not be split.
     */
    private static TextSegment splitAndGetNext(final TextSegment it, final String parameter) {
        final var start = it.text.indexOf(parameter);
        if (start < 0) {
            return null;
        }
        final var textSeg = new TextSegment(it.nextSeg, it.text.substring(start + parameter.length()));
        it.nextSeg = new ParameterSegment(textSeg, parameter);
        it.text = it.text.substring(0, start);
        return textSeg;
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
     * <p>
     * The catch is that before returning the last text segment is also split for the same parameter. That way the
     * segment originally being
     *
     * <pre>
     *     bla bla ... P ... bla bla .... P ... bla bla... P ... bla
     * </pre>
     * <p>
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
     * <p>
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
        split(this, parameter);
    }

}