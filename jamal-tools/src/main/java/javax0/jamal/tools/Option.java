package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.api.ObjectHolder;

/**
 * Options are stored as user defined macros. Note that the macro storage the processor manages allows anything
 * implementing the {@link Identified} interface to be stored in the storage. An option represents a boolean value that
 * the {@code options} ,acro can set or reset. This object also implement the {@link ObjectHolder} interface so it can be
 * used by some of the macros that rely on object storing of other macros.
 */
public class Option implements Evaluable, ObjectHolder<Boolean> {
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

    @Override
    public String evaluate(String... parameters) throws BadSyntax {
        return Boolean.toString(value);
    }

    @Override
    public int expectedNumberOfArguments() {
        return 0;
    }
}
