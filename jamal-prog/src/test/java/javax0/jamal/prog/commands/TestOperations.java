package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("All the operations work on strings as expected")
public class TestOperations {

    @Nested
    class OperationAdd {
        @Test
        @DisplayName("The add operation works on strings that contain numbers")
        void addNumbers() throws BadSyntax {
            final var op = new Operation("+", new Constant("1"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("3", result);
        }

        @Test
        @DisplayName("The add operation works on strings concatenating when one of the operands is not a number")
        void concatStrings() throws BadSyntax {
            final var op = new Operation("+", new Constant("1"), new Constant("a"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("1a", result);
        }

        @Test
        @DisplayName("The add operation works on strings concatenating when both operands are not numbers")
        void concatStrings2() throws BadSyntax {
            final var op = new Operation("+", new Constant("a"), new Constant("b"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("ab", result);
        }

        @Test
        @DisplayName("The add operation works when the first operand is null")
        void addNull() throws BadSyntax {
            final var op = new Operation("+", null, new Constant("a"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("a", result);
        }
    }

    @Nested
    class OperationMinus {

        @DisplayName("The minus operation works on strings that contain numbers")
        @Test
        void minusNumbers() throws BadSyntax {
            final var op = new Operation("-", new Constant("1"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("-1", result);
        }

        @DisplayName("The minus operation works on number when the first operand is null")
        @Test
        void minusNull() throws BadSyntax {
            final var op = new Operation("-", null, new Constant("1"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("-1", result);
        }

        @DisplayName("The minus operation works on number when the first operand is null")
        @Test
        void minusNull2() throws BadSyntax {
            final var op = new Operation("-", null, new Constant("a"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("-a", result);
        }

        @DisplayName("The minus operation throws up when the first operand is string")
        @Test
        void minusString() {
            final var op = new Operation("-", new Constant("a"), new Constant("1"));
            Assertions.assertThrows(BadSyntax.class, () -> op.execute(new Context(null)));
        }

        @DisplayName("The minus operation throws up when the second operand is string")
        @Test
        void minusString2() {
            final var op = new Operation("-", new Constant("1"), new Constant("a"));
            Assertions.assertThrows(BadSyntax.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationMultiply {

        @DisplayName("The multiply operation works on strings that contain numbers")
        @Test
        void multiplyNumbers() throws BadSyntax {
            final var op = new Operation("*", new Constant("1"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("2", result);
        }

        @DisplayName("The multiply operation throws up when the first operand is null")
        @Test
        void multiplyNull() {
            final var op = new Operation("*", null, new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }

        @DisplayName("The multiply operation throws when the first operand is null")
        @Test
        void multiplyNull2() {
            final var op = new Operation("*", null, new Constant("a"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }

        @DisplayName("The multiply operation throws up when the first operand is string")
        @Test
        void multiplyString() {
            final var op = new Operation("*", new Constant("a"), new Constant("1"));
            Assertions.assertThrows(BadSyntax.class, () -> op.execute(new Context(null)));
        }

        @DisplayName("The multiply operation throws up when the second operand is string")
        @Test
        void multiplyString2() {
            final var op = new Operation("*", new Constant("1"), new Constant("a"));
            Assertions.assertThrows(BadSyntax.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationDivide {

        @DisplayName("The divide operation works on strings that contain numbers")
        @Test
        void divideNumbers() throws BadSyntax {
            final var op = new Operation("/", new Constant("10"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("5", result);
        }

        @DisplayName("The divide operation throws up when the first operand is null")
        @Test
        void divideNull() {
            final var op = new Operation("/", null, new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }

        @DisplayName("The divide operation throws when the first operand is null")
        @Test
        void divideNull2() {
            final var op = new Operation("/", null, new Constant("a"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }

        @DisplayName("The divide operation throws up when the first operand is string")
        @Test
        void divideString() {
            final var op = new Operation("/", new Constant("a"), new Constant("1"));
            Assertions.assertThrows(BadSyntax.class, () -> op.execute(new Context(null)));
        }

        @DisplayName("The divide operation throws up when the second operand is string")
        @Test
        void divideString2() {
            final var op = new Operation("/", new Constant("1"), new Constant("a"));
            Assertions.assertThrows(BadSyntax.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationModulo {

        @DisplayName("The modulo operation works on strings that contain numbers")
        @Test
        void moduloNumbers() throws BadSyntax {
            final var op = new Operation("%", new Constant("10"), new Constant("3"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("1", result);
        }

        @DisplayName("The modulo operation throws up when the first operand is null")
        @Test
        void moduloNull() {
            final var op = new Operation("%", null, new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }

        @DisplayName("The modulo operation throws when the first operand is null")
        @Test
        void moduloNull2() {
            final var op = new Operation("%", null, new Constant("a"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }

        @DisplayName("The modulo operation throws up when the first operand is string")
        @Test
        void moduloString() {
            final var op = new Operation("%", new Constant("a"), new Constant("1"));
            Assertions.assertThrows(BadSyntax.class, () -> op.execute(new Context(null)));
        }

        @DisplayName("The modulo operation throws up when the second operand is string")
        @Test
        void moduloString2() {
            final var op = new Operation("%", new Constant("1"), new Constant("a"));
            Assertions.assertThrows(BadSyntax.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationLessThan {
        @DisplayName("The less than operation works on strings that contain numbers")
        @Test
        void lessThanNumbers() throws BadSyntax {
            final var op = new Operation("<", new Constant("1"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The less than operation works on strings")
        @Test
        void lessThanStrings() throws BadSyntax {
            final var op = new Operation("<", new Constant("a"), new Constant("b"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The less than operation throws up when the first operand is null")
        @Test
        void lessThanNull() {
            final var op = new Operation("<", null, new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationLessThanOrEqual {
        @DisplayName("The less than or equal operation works on strings that contain numbers")
        @Test
        void lessThanOrEqualNumbers() throws BadSyntax {
            final var op = new Operation("<=", new Constant("1"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The less than or equal operation works on strings")
        @Test
        void lessThanOrEqualStrings() throws BadSyntax {
            final var op = new Operation("<=", new Constant("a"), new Constant("b"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The less than or equal operation throws up when the first operand is null")
        @Test
        void lessThanOrEqualNull() {
            final var op = new Operation("<=", null, new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationGreaterThan {
        @DisplayName("The greater than operation works on strings that contain numbers")
        @Test
        void greaterThanNumbers() throws BadSyntax {
            final var op = new Operation(">", new Constant("1"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("false", result);
        }

        @DisplayName("The greater than operation works on strings")
        @Test
        void greaterThanStrings() throws BadSyntax {
            final var op = new Operation(">", new Constant("a"), new Constant("b"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("false", result);
        }

        @DisplayName("The greater than operation throws up when the first operand is null")
        @Test
        void greaterThanNull() {
            final var op = new Operation(">", null, new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationGreaterThanOrEqual {
        @DisplayName("The greater than or equal operation works on strings that contain numbers")
        @Test
        void greaterThanOrEqualNumbers() throws BadSyntax {
            final var op = new Operation(">=", new Constant("1"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("false", result);
        }

        @DisplayName("The greater than or equal operation works on strings")
        @Test
        void greaterThanOrEqualStrings() throws BadSyntax {
            final var op = new Operation(">=", new Constant("a"), new Constant("b"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("false", result);
        }

        @DisplayName("The greater than or equal operation throws up when the first operand is null")
        @Test
        void greaterThanOrEqualNull() {
            final var op = new Operation(">=", null, new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationEqual {
        @DisplayName("The equal operation works on strings that contain numbers")
        @Test
        void equalNumbers() throws BadSyntax {
            final var op = new Operation("==", new Constant("1"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("false", result);
        }

        @DisplayName("The equal operation works on strings")
        @Test
        void equalStrings() throws BadSyntax {
            final var op = new Operation("==", new Constant("a"), new Constant("b"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("false", result);
        }

        @DisplayName("The equal operation throws up when the first operand is null")
        @Test
        void equalNull() {
            final var op = new Operation("==", null, new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationNotEqual {
        @DisplayName("The equal operation works on strings that contain numbers")
        @Test
        void equalNumbers() throws BadSyntax {
            final var op = new Operation("!=", new Constant("1"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The equal operation works on strings")
        @Test
        void equalStrings() throws BadSyntax {
            final var op = new Operation("!=", new Constant("a"), new Constant("b"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The equal operation throws up when the first operand is null")
        @Test
        void equalNull() {
            final var op = new Operation("!=", null, new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationAnd {
        @DisplayName("The and operation works on strings that contain numbers")
        @Test
        void andNumbers() throws BadSyntax {
            final var op = new Operation("and", new Constant("1"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The and operation works on strings")
        @Test
        void andStrings() throws BadSyntax {
            final var op = new Operation("and", new Constant("true"), new Constant("tRuE"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The and operation throws up when the first operand is null")
        @Test
        void andNull() {
            final var op = new Operation("and", null, new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationOr {
        @DisplayName("The or operation works on strings that contain numbers")
        @Test
        void orNumbers() throws BadSyntax {
            final var op = new Operation("or", new Constant("1"), new Constant("2"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The or operation works on strings")
        @Test
        void orStrings() throws BadSyntax {
            final var op = new Operation("or", new Constant("False"), new Constant("abraka dabraka"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The or operation throws up when the first operand is null")
        @Test
        void orNull() {
            final var op = new Operation("or", null, new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }
    }

    @Nested
    class OperationNot {
        @DisplayName("The not operation works on string")
        @Test
        void notString() throws BadSyntax {
            final var op = new Operation("not", null, new Constant("False"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The not operation works on number")
        @Test
        void notNumber() throws BadSyntax {
            final var op = new Operation("not", null, new Constant("0"));
            final var result = op.execute(new Context(null));
            Assertions.assertEquals("true", result);
        }

        @DisplayName("The not operation throws up when the first operand is not null")
        @Test
        void notNull() {
            final var op = new Operation("not", new Constant("1"), new Constant("1"));
            Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
        }
    }

    @DisplayName("The operation throws up when the operator is not supported")
    @Test
    void notSupported() {
        final var op = new Operation("not supported", null, new Constant("1"));
        Assertions.assertThrows(RuntimeException.class, () -> op.execute(new Context(null)));
    }
}
