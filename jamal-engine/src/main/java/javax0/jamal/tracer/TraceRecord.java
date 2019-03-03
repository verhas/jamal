package javax0.jamal.tracer;

public interface TraceRecord {
    TraceRecord sourceAppend(String string) ;
    TraceRecord targetAppend(String string) ;
    String source();
    String target();
    int level();
}
