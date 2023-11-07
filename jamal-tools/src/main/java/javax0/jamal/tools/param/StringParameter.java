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
        return this;
    }

    public StringParameter optional() {
        return defaultValue(null);
    }

    public StringParameter defaultValue(String dV) {
        if (dV == null) {
            param.orElseNull();
        } else {
            param.defaultValue(dV);
        }
        return this;
    }
}
