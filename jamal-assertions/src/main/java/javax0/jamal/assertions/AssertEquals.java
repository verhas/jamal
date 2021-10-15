package javax0.jamal.assertions;

public class AssertEquals extends AbstractAssert {

    public AssertEquals() {
        super(3,"'%s' does not equal '%s'", "'%s' equals '%s'");
    }

    protected boolean test(String[] parts) {
        return parts[0].equals(parts[1]);
    }

    private static final String[] IDS = {"assert:equals","assert:equal"};
    @Override
    public String[] getIds(){
        return IDS;
    }
}
