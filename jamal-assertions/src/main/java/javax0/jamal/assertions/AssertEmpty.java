package javax0.jamal.assertions;

public class AssertEmpty extends AbstractAssert {

    public AssertEmpty() {
        super(2, "'%s' is not empty", "value is empty");
    }

    @Override
    protected boolean test(String[] parts) {
        return parts[0].length() == 0;
    }
}
