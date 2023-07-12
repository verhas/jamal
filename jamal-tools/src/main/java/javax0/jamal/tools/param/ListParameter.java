package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Params;

import java.util.List;

public class ListParameter extends AbstractTypedParameter<List<String>> {

    public ListParameter(Params.Param<List<String>> param) {
        super(param);
    }

    public List<String> get() throws BadSyntax {
        return param.get();
    }
}
