package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Params;

public class StringParameter extends AbstractTypedParameter<String> {

    public StringParameter(Params.Param<String> param) {
        super(param);
    }

    public String get() throws BadSyntax {
        return param.get();
    }


    public StringParameter required() {
        checkDone(DoneAction.REQUIRED);
        return this;
    }

    public StringParameter optional() {
        checkDone(DoneAction.OPTIONAL);
        param.orElseNull();
        return this;
    }

    public StringParameter defaultValue(String dV) {
        checkDone(DoneAction.DEFAULT);
        if (dV == null) {
            param.orElseNull();
        } else {
            param.defaultValue(dV);
        }
        return this;
    }
}
