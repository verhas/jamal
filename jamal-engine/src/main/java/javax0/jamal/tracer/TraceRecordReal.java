package javax0.jamal.tracer;

import javax0.jamal.api.Position;

public class TraceRecordReal implements TraceRecord {
    private final int level;
    private final StringBuilder source = new StringBuilder();
    private final StringBuilder target = new StringBuilder();
    private Position position;
    private String type;
    private boolean hasOutput = false;

    public TraceRecordReal(int level) {
        this.level = level;
    }

    @Override
    public TraceRecordReal sourceAppend(String string) {
        source.append(string);
        return this;
    }

    @Override
    public TraceRecordReal targetAppend(String string) {
        target.append(string);
        hasOutput = true;
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

    @Override
    public Position position() {
        return position;
    }

    @Override
    public void position(Position position) {
        this.position = position;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public void type(String type) {
        this.type = type;
    }

    @Override
    public boolean hasOutput() {
        return hasOutput;
    }
}
