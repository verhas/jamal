package javax0.jamal.assertions;

public class AssertContains extends AbstractAssert {

    public AssertContains() {
        super(3,"'%s' does not contain '%s'", "'%s' contains '%s'");
    }

    protected boolean test(String[] parts) {
        return parts[0].contains(parts[1]);
    }
}
