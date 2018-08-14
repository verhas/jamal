package javax0.jamal.engine.macro;

public class TextSegment extends Segment {
    public TextSegment(Segment next, String content){
        this.nextSeg = next;
        this.text = content;
    }
    public void split(String parameter, String value) {
        var start = text.indexOf(parameter);
        if (start != -1) {
            var textSeg = new TextSegment(nextSeg, text.substring(start + parameter.length()));
            textSeg.split(parameter, value);
            var parSeg = new ParameterSegment();
            parSeg.text = value;
            parSeg.nextSeg = textSeg;
            text = text.substring(0, start);
            nextSeg = parSeg;
        }
    }
}