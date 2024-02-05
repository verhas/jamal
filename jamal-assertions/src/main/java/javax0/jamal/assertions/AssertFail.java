package javax0.jamal.assertions;

import javax0.jamal.api.Macro;

@Macro.Name("assert:fail")
public class AssertFail extends AbstractAssert {

    public AssertFail() {
        super(1, "", "will never display");
    }

    @Override
    protected boolean test(String[] parts) {
        return false;
    }
}
