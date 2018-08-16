package javax0.jamal.api;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public interface Macro {
    static List<Macro> getInstances() {
        ServiceLoader<Macro> services = ServiceLoader.load(Macro.class);
        List<Macro> list = new ArrayList<>();
        services.iterator().forEachRemaining(list::add);
        return list;
    }

    String evaluate(StringBuilder input, Processor processor) throws BadSyntax;

    default String getId() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}
