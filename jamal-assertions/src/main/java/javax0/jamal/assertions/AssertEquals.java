package javax0.jamal.assertions;

import javax0.jamal.api.Macro;

@Macro.Name({"assert:equals","assert:equal"})
public class AssertEquals extends AbstractAssert {

    public AssertEquals() {
        super(3,"'%s' does not equal '%s'", "'%s' equals '%s'");
    }

    protected boolean test(String[] parts) {
        return parts[0].equals(parts[1]);
    }
}
