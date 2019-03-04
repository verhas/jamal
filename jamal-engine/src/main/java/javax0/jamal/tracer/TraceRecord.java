package javax0.jamal.tracer;

import javax0.jamal.api.Position;

public interface TraceRecord {
    TraceRecord sourceAppend(String string);

    TraceRecord targetAppend(String string);

    String source();

    String target();

    int level();

    String type();

    void type(String type);

    Position position();

    void position(Position position);

    boolean hasOutput();
}
