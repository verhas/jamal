package javax0.jamal.tracer;

import javax0.jamal.api.Position;

import java.util.ArrayList;
import java.util.List;

public class TraceRecordReal implements TraceRecord {
    private final int level;
    private final StringBuilder before = new StringBuilder();
    private final StringBuilder evaluated = new StringBuilder();
    private final StringBuilder result = new StringBuilder();
    private final List<TraceRecord> subRecords = new ArrayList<>();
    private final TraceRecordFactory myFactory;
    private Position position;
    private TraceRecord.Type type;
    private boolean hasOutput = false;
    private String id = "";


    public TraceRecordReal(int level, TraceRecordFactory myFactory) {
        this.level = level;
        this.myFactory = myFactory;
    }

    @Override
    public void close() {
        myFactory.pop();
    }

    @Override
    public TraceRecordReal appendBeforeState(String string) {
        before.append(string);
        return this;
    }

    @Override
    public TraceRecord appendAfterEvaluation(String string) {
        evaluated.append(string);
        return this;
    }

    @Override
    public TraceRecordReal appendResultState(String string) {
        result.append(string);
        hasOutput = true;
        return this;
    }

    @Override
    public TraceRecord subRecord(Type type) {
        final var record = new TraceRecordReal(level + 1, myFactory);
        subRecords.add(record);
        return record;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        if (id != null) {
            this.id = id;
        }
    }

    @Override
    public List<TraceRecord> getSubRecords() {
        return subRecords;
    }

    @Override
    public String source() {
        return before.toString();
    }

    @Override
    public String target() {
        return result.toString();
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
    public TraceRecord.Type type() {
        return type;
    }

    @Override
    public void type(TraceRecord.Type type) {
        this.type = type;
    }

    @Override
    public boolean hasOutput() {
        return hasOutput;
    }
}
