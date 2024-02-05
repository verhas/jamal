package javax0.jamal.assertions;

import javax0.jamal.api.Macro;

@Macro.Name({"assert:endsWith", "assert:endswith"})
public class AssertEndsWith extends AbstractAssert {

    public AssertEndsWith() {
        super(3,"'%s' does not end with '%s'", "'%s' ends with '%s'");
    }

    protected boolean test(String[] parts) {
        return parts[0].endsWith(parts[1]);
    }
}
