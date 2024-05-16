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
        checkDone(DoneAction.REQUIRED);
        return this;
    }

    public IntegerParameter optional() {
        checkDone(DoneAction.OPTIONAL);
        param.orElseNull();
        return this;
    }

    public IntegerParameter defaultValue(Integer dV) {
        checkDone(DoneAction.DEFAULT);
        if (dV == null) {
            param.orElseNull();
        } else {
            param.defaultValue(dV);
        }
        return this;
    }
}
