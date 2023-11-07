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
        return this;
    }

    public PatternParameter optional() {
        param.orElseNull();
        return this;
    }

    public PatternParameter defaultValue(String dV) {
        if (dV == null) {
            param.orElseNull();
        } else {
            param.defaultValue(dV);
        }
        return this;
    }
}
