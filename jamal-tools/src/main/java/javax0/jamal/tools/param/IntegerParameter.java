package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Params;

public class IntegerParameter extends AbstractTypedParameter<Integer> {

    public IntegerParameter(Params.Param<Integer> param) {
        super(param);
    }

    public int get() throws BadSyntax {
        return param.get();
    }

    public IntegerParameter required() {
        return this;
    }

    public IntegerParameter optional() {
        return defaultValue(0);
    }

    public IntegerParameter defaultValue(Integer dV) {
        if (dV == null) {
            param.orElseNull();
        } else {
            param.defaultValue(dV);
        }
        return this;
    }
}
