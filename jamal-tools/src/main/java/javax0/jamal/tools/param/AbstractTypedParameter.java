package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
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


    public AbstractTypedParameter(Params.Param<T> param) {
        this.param = param;
    }

    public boolean isPresent() throws BadSyntax {
        return param.isPresent();
    }

    public String name() {
        return param.name();
    }
}
