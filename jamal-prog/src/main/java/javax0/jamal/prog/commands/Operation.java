package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.InputHandler;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Predicate;

public class Operation extends Expression {
    final String operator;
    final Expression left;
    final Expression right;

    public Operation(final String operator, final Expression left, final Expression right) {
        this.operator = Objects.requireNonNull(operator);
        this.left = left;
        this.right = Objects.requireNonNull(right);
    }

    @Override
    public String execute(final Context ctx) throws BadSyntax {
        ctx.step();
        final String leftValue;

        if (left != null) {
            leftValue = left.execute(ctx);
        } else {
            leftValue = null;
        }
        final String rightValue;
        switch (operator) {
            case "+":
                rightValue = right.execute(ctx);
                if (leftValue == null) {
                    return rightValue;
                }
                if (bothNumeric(leftValue, rightValue)) {
                    return new BigInteger(leftValue).add(new BigInteger(rightValue)).toString();
                }
                return leftValue + rightValue;
            case "-":
                rightValue = right.execute(ctx);
                if (leftValue == null) {
                    return "-" + rightValue;
                }
                assertBothNumeric(leftValue, rightValue, operator);
                return new BigInteger(leftValue).subtract(new BigInteger(rightValue)).toString();
            case "*":
                rightValue = right.execute(ctx);
                assertBothNumeric(leftValue, rightValue, operator);
                return new BigInteger(leftValue).multiply(new BigInteger(right.execute(ctx))).toString();
            case "/":
                rightValue = right.execute(ctx);
                assertBothNumeric(leftValue, rightValue, operator);
                return new BigInteger(leftValue).divide(new BigInteger(right.execute(ctx))).toString();
            case "%":
                rightValue = right.execute(ctx);
                assertBothNumeric(leftValue, rightValue, operator);
                return new BigInteger(leftValue).mod(new BigInteger(rightValue)).toString();
            case "<":
                return compare(ctx, leftValue, right, x -> x < 0);
            case "<=":
                return compare(ctx, leftValue, right, x -> x <= 0);
            case ">":
                return compare(ctx, leftValue, right, x -> x > 0);
            case ">=":
                return compare(ctx, leftValue, right, x -> x >= 0);
            case "==":
                return compare(ctx, leftValue, right, x -> x == 0);
            case "!=":
                return compare(ctx, leftValue, right, x -> x != 0);
            case "and":
                assertNotNull(leftValue, operator);
                return (isTrue(leftValue) && isTrue(right.execute(ctx))) + "";
            case "or":
                assertNotNull(leftValue, operator);
                return (isTrue(leftValue) || isTrue(right.execute(ctx))) + "";
            case "!":
                assertNull(leftValue, operator);
                return ctx.getProcessor().process(Input.makeInput(right.execute(ctx)));
            case "not":
                assertNull(leftValue, operator);
                return (!isTrue(right.execute(ctx))) + "";
            default:
                throw new RuntimeException("Unknown operator '" + operator + "'");
        }
    }

    private static String compare(final Context ctx,
                                  final String leftValue,
                                  final Expression rightValue,
                                  final Predicate<Integer> predicate) throws BadSyntax {
        if (leftValue == null) {
            throw new RuntimeException("SNAFU Left value of comparison is null");
        }
        final var b = rightValue.execute(ctx);
        if (bothNumeric(leftValue, b)) {
            return predicate.test(getCompareTo(leftValue, b)) + "";
        }
        return predicate.test(leftValue.compareTo(b)) + "";
    }

    private static void assertBothNumeric(final String leftValue, final String rightValue, final String operator) throws BadSyntax {
        assertNotNull(leftValue, operator);
        if (!bothNumeric(leftValue, rightValue)) {
            throw new BadSyntax(String.format("Cannot do '%s' on non-numeric values", operator));
        }
    }

    private static void assertNull(final String leftValue, final String operator) {
        if (leftValue != null) {
            throw new RuntimeException(String.format("SNAFU left value for '%s' is not null", operator));
        }
    }

    private static void assertNotNull(final String leftValue, final String operator) {
        if (leftValue == null) {
            throw new RuntimeException(String.format("SNAFU left value for '%s' is null", operator));
        }
    }

    static boolean bothNumeric(final String leftValue, final String rightValue) {
        return InputHandler.isNumber(leftValue) && InputHandler.isNumber(rightValue);
    }

    private static int getCompareTo(final String leftValue, final String rightValue) {
        return new BigInteger(leftValue).compareTo(new BigInteger(rightValue));
    }

    public static boolean isTrue(final String test) {
        if (test.trim().equalsIgnoreCase("false")) {
            return false;
        }
        if (test.trim().equalsIgnoreCase("true")) {
            return true;
        }
        if (test.trim().matches("[+-]?\\d+")) {
            return new BigInteger(test).compareTo(BigInteger.ZERO) != 0;
        }
        return test.trim().length() > 0;
    }

}
