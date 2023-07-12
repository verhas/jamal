package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Params;

public class BooleanParameter extends AbstractTypedParameter {

    public BooleanParameter(Params.Param<Boolean> param) {
        super(param);
    }

    public boolean is() throws BadSyntax {
        return param.is();
    }

}
