package javax0.jamal.assertions;

import javax0.jamal.api.BadSyntax;

public abstract class NumericAsserts extends AbstractAssert {
    private NumericAsserts(String defaultMessage, String negatedDefaultMessage) {
        super(3, defaultMessage, negatedDefaultMessage);
    }

    abstract protected boolean test(int p0, int p1);

    @Override
    protected boolean test(String[] parts) throws BadSyntax {
        final int p0, p1;
        try {
            p0 = Integer.parseInt(parts[0]);
        } catch (NumberFormatException nfe) {
            throw new BadSyntax(
                    String.format("The parameter in " + this.getId() + " is not a well formatted integer: '%s'", parts[0]));
        }
        try {
            p1 = Integer.parseInt(parts[1]);
        } catch (NumberFormatException nfe) {
            throw new BadSyntax(
                    String.format("The parameter in " + this.getId() + " is not a well formatted integer: '%s'", parts[1]));
        }
        return test(p0, p1);
    }

    @Name({"assert:intEquals", "assert:intEqual"})
    public static class AssertIntEquals extends NumericAsserts {
        public AssertIntEquals() {
            super("'%s' does not equal '%s'", "'%s' equals '%s'");
        }

        protected boolean test(int a, int b) {
            return a == b;
        }
    }

    @Name("assert:less")
    public static class AssertLess extends NumericAsserts {
        public AssertLess() {
            super("'%s' is not less '%s'", "'%s' is less '%s'");
        }

        protected boolean test(int a, int b) {
            return a < b;
        }
    }

    @Name({"assert:lessOrEquals", "assert:lessOrEqual"})
    public static class AssertLessOrEqual extends NumericAsserts {
        public AssertLessOrEqual() {
            super("'%s' is not less or equal '%s'", "'%s' is less or equal '%s'");
        }

        protected boolean test(int a, int b) {
            return a <= b;
        }
    }

    @Name("assert:greater")

    public static class AssertGreater extends NumericAsserts {
        public AssertGreater() {
            super("'%s' is not greater '%s'", "'%s' is greater '%s'");
        }

        protected boolean test(int a, int b) {
            return a > b;
        }
    }

    @Name({"assert:greaterOrEquals", "assert:greaterOrEqual"})
    public static class AssertGreaterOrEqual extends NumericAsserts {
        public AssertGreaterOrEqual() {
            super("'%s' is not greater or equal '%s'", "'%s' is greater or equal '%s'");
        }

        protected boolean test(int a, int b) {
            return a >= b;
        }
    }

    @Name("assert:numeric")
    public static class AssertNumeric extends AbstractAssert {
        public AssertNumeric() {
            super(2, "'%s' not numeric", "'%s' numeric");
        }

        @Override
        protected boolean test(String[] parts) {
            try {
                Double.parseDouble(parts[0]);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        }
    }

    @Name("assert:int")
    public static class AssertInt extends AbstractAssert {
        public AssertInt() {
            super(2, "'%s' not numeric", "'%s' numeric");
        }

        @Override
        protected boolean test(String[] parts) {
            try {
                Integer.parseInt(parts[0]);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        }
    }

}
