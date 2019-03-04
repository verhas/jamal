package javax0.jamal.tracer;

import javax0.jamal.api.Position;

public class TraceRecordNull implements TraceRecord {
    @Override
    public TraceRecord sourceAppend(String string) {
        return this;
    }

    @Override
    public TraceRecord targetAppend(String string) {
        return this;
    }

    @Override
    public String source() {
        return null;
    }

    @Override
    public String target() {
        return null;
    }

    @Override
    public int level() {
        return 0;
    }

    @Override
    public Position position() {
        return null;
    }

    @Override
    public void position(Position position) {
    }

    @Override
    public String type() {
        return null;
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void type(String type) {
    }
}
