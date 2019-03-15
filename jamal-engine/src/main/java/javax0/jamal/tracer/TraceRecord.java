package javax0.jamal.tracer;

import javax0.jamal.api.Position;

import java.util.List;

public interface TraceRecord extends AutoCloseable {
    TraceRecord appendBeforeState(String string);

    TraceRecord appendAfterEvaluation(String string);

    TraceRecord appendResultState(String string);

    TraceRecord subRecord(Type type);

    String getId();

    void setId(String id);

    void setParameters(String[] parameters);
    String[] getParameters();

    List<TraceRecord> getSubRecords();

    String beforeState();

    String evaluatedState();

    String resultState();

    int level();

    Type type();

    void type(Type type);

    Position position();

    void position(Position position);

    boolean hasOutput();

    void close();

    enum Type {
        TEXT, USER_DEFINED_MACRO, MACRO
    }
}
