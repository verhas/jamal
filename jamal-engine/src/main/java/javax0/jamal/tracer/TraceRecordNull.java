package javax0.jamal.tracer;

import javax0.jamal.api.Position;

import java.util.List;

public class TraceRecordNull implements TraceRecord {
    @Override
    public TraceRecord appendBeforeState(String string) {
        return this;
    }

    @Override
    public TraceRecord appendAfterEvaluation(String string) {
        return null;
    }

    @Override
    public TraceRecord appendResultState(String string) {
        return this;
    }

    @Override
    public TraceRecord subRecord(Type type) {
        return this;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void setId(String id) {

    }

    @Override
    public List<String> getWarnings() {
        return List.of();
    }

    @Override
    public String[] getParameters() {
        return new String[0];
    }

    @Override
    public void setParameters(String[] parameters) {

    }

    @Override
    public List<TraceRecord> getSubRecords() {
        return null;
    }

    @Override
    public String beforeState() {
        return null;
    }

    @Override
    public String evaluatedState() {
        return null;
    }

    @Override
    public String resultState() {
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
    public TraceRecord.Type type() {
        return null;
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void warning(String s) {
    }

    @Override
    public void type(TraceRecord.Type type) {
    }

    @Override
    public void close() {

    }
}
