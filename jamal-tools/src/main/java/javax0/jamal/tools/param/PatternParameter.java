package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Params;

import java.util.regex.Pattern;

public class PatternParameter extends AbstractTypedParameter<Pattern> {

    public PatternParameter(Params.Param<Pattern> param) {
        super(param);
    }

    public Pattern get() throws BadSyntax {
        return param.get();
    }

    public PatternParameter required() {
        checkDone(DoneAction.REQUIRED);
        return this;
    }

    public PatternParameter optional() {
        checkDone(DoneAction.OPTIONAL);
        param.orElseNull();
        return this;
    }

    public PatternParameter defaultValue(String dV) {
        checkDone(DoneAction.DEFAULT);
        if (dV == null) {
            param.orElseNull();
        } else {
            param.defaultValue(dV);
        }
        return this;
    }
}
