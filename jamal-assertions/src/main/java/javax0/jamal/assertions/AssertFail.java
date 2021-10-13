package javax0.jamal.assertions;

public class AssertFail extends AbstractAssert {

    public AssertFail() {
        super(1, "", "will never display");
    }

    @Override
    protected boolean test(String[] parts) {
        return false;
    }
}
