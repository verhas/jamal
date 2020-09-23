package javax0.jamal.tracer;

import javax0.jamal.api.Input;
import javax0.jamal.api.Position;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TraceRecordFactory {
    final List<TraceRecord> traces = new ArrayList<>();
    final Deque<TraceRecord> stack = new ArrayDeque<>();
    private final String traceFile;
    /**
     * Contains the level of execution during the evaluation. Since the implementation of macro evaluation is
     * implemented in a recursive way this counter is maintained in the method {@code javax0.jamal.api.Processor#process(Input)}.
     */
    private int level = 0;

    public TraceRecordFactory() {
        final var sysProp = System.getProperty("jamal.trace");
        if (sysProp != null) {
            traceFile = sysProp;
        } else {
            traceFile = System.getenv("JAMAL_TRACE");
        }
    }

    public TraceRecord openUserDefinedMacroRecord(Position position) {
        final var record = openTraceRecord(position, TraceRecord.Type.USER_DEFINED_MACRO);
        record.type(TraceRecord.Type.USER_DEFINED_MACRO);
        return record;
    }


    public TraceRecord openMacroRecord(Position position) {
        final var record = openTraceRecord(position, TraceRecord.Type.MACRO);
        record.type(TraceRecord.Type.MACRO);
        return record;
    }

    public TraceRecord openTextRecord(Position position) {
        return openTraceRecord(position, TraceRecord.Type.TEXT);
    }

    private TraceRecord openTraceRecord(Position position, TraceRecord.Type t) {
        if (traceFile == null) {
            return new TraceRecordNull();
        } else {
            level++;
            final TraceRecord traceRecord;
            if (stack.isEmpty()) {
                traceRecord = new TraceRecordReal(level, this);
            } else {
                traceRecord = stack.getLast().subRecord(t);
            }
            if (stack.isEmpty()) {
                traces.add(traceRecord);
            }
            stack.add(traceRecord);
            traceRecord.type(t);
            traceRecord.position(position);
            return traceRecord;
        }
    }

    void pop() {
        level--;
        stack.removeLast();
    }

    public void dump(Exception ex) {
        if (level == 0 && traceFile != null) {
            new TraceDumper().dump(traces, traceFile, ex);
        }
    }
}
