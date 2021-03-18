package javax0.jamal.api;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public interface Debugger extends AutoCloseable {
    String JAMAL_DEBUG = "JAMAL_DEBUG";
    static List<Debugger> getInstances() {
        ServiceLoader<Debugger> services = ServiceLoader.load(Debugger.class);
        List<Debugger> list = new ArrayList<>();
        services.iterator().forEachRemaining(list::add);
        return list;
    }

    interface Stub {
        List<Debuggable.Scope> getScopeList();

        void process(String in) throws BadSyntax;
    }

    void setBefore(int level, CharSequence input);

    void setStart(CharSequence macro);

    void setAfter(int level, CharSequence input, CharSequence output);

    void close();

    int affinity(String s);

    void init(Stub stub) throws Exception;
}
