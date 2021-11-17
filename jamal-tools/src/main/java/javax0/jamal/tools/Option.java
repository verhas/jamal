package javax0.jamal.tools;

import javax0.jamal.api.Identified;
import javax0.jamal.api.ObjectHolder;

public class Option implements Identified, ObjectHolder<Boolean> {
    private final String name;
    private boolean value;
    public Option(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public Boolean getObject() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }
}
