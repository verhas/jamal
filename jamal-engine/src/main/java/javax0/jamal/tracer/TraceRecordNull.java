package javax0.jamal.tracer;

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
}
