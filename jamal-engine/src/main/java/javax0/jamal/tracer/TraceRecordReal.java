package javax0.jamal.tracer;

import javax0.jamal.api.Position;

public class TraceRecordReal implements TraceRecord {
    private final int level;
    private final StringBuilder source = new StringBuilder();
    private final StringBuilder target = new StringBuilder();
    private Position position;

    public TraceRecordReal(int level) {
        this.level = level;
    }

    public TraceRecordReal sourceAppend(String string) {
        source.append(string);
        return this;
    }

    public TraceRecordReal targetAppend(String string) {
        target.append(string);
        return this;
    }

    @Override
    public String source() {
        return source.toString();
    }

    @Override
    public String target() {
        return target.toString();
    }

    @Override
    public int level() {
        return level;
    }
}
