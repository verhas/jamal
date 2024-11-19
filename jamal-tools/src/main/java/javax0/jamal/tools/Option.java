package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.api.ObjectHolder;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Options are stored as user defined macros. Note that the macro storage the processor manages allows anything
 * implementing the {@link Identified} interface to be stored in the storage. An option represents a boolean value that
 * the {@code options} ,acro can set or reset. This object also implement the {@link ObjectHolder} interface so it can be
 * used by some of the macros that rely on object storing of other macros.
 */
public class Option implements Evaluable, ObjectHolder<Boolean> {
    private final String name;
    private boolean value;

    private final Deque<Boolean> history = new ArrayDeque<>();

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

    public void push(final boolean value) {
        history.push(this.value);
        this.value = value;
    }
    public void pop() throws BadSyntax {
        BadSyntax.when(history.isEmpty(), "Cannot pop from empty option stack for option '%s'", name);
        this.value = history.pop();
    }
    public void set(boolean value) {
        this.value = value;
    }

    @Override
    public String evaluate(String... parameters) {
        return Boolean.toString(value);
    }

    @Override
    public int expectedNumberOfArguments() {
        return 0;
    }
}
