package javax0.jamal.tools.param;

import javax0.jamal.tools.Params;

public abstract class AbstractTypedParameter<T> {
    protected final Params.Param<T> param;

    /**
     * For compatibility reasons, sometimes you may need access to the underlying parameter.
     *
     * @return the underlying parameter
     */
    public Params.Param<T> getParam() {
        return param;
    }

    private boolean done = false;

    public AbstractTypedParameter(Params.Param<T> param) {
        this.param = param;
    }

    void checkDone(final DoneAction action) {
        if (done) {
            throw new IllegalStateException("Not possible to " + action
                    + "  when the parameter is already defined as required, optional or default value is set.");
        }
        done = true;
    }

    public boolean isPresent() {
        return param.isPresent();
    }

    /**
     * @return the name, which was actually used for the parameter. It is the same string as the name of the
     * parameter or one of the aliases.
     * <p>
     * If the parameter is multi-values then the name used the last time will be returned.
     */
    public String name() {
        return param.name();
    }
}
