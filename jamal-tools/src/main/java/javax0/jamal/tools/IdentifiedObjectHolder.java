package javax0.jamal.tools;

import javax0.jamal.api.Identified;
import javax0.jamal.api.ObjectHolder;

public class IdentifiedObjectHolder<T> implements ObjectHolder<T>, Identified   {

    private final T object;
    private final String id;

    public IdentifiedObjectHolder(final T object, final String id) {
        this.object = object;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public T getObject() {
        return object;
    }
}
