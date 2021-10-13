package javax0.jamal.assertions;

public class AssertStartsWith extends AbstractAssert {

    public AssertStartsWith() {
        super(3,"'%s' does not start with '%s'", "'%s' starts with '%s'");
    }

    protected boolean test(String[] parts) {
        return parts[0].startsWith(parts[1]);
    }
}
