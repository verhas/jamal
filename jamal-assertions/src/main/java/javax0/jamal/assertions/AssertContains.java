package javax0.jamal.assertions;

import javax0.jamal.api.Macro;

@Macro.Name("assert:contains")
public class AssertContains extends AbstractAssert {

    public AssertContains() {
        super(3,"'%s' does not contain '%s'", "'%s' contains '%s'");
    }

    protected boolean test(String[] parts) {
        return parts[0].contains(parts[1]);
    }
}
