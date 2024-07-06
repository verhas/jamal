package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.InputHandler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Operation extends Expression {
    final String operator;
    final Expression left;
    final Expression right;

    public Operation(final String operator, final Expression left, final Expression right) {
        this.operator = Objects.requireNonNull(operator);
        this.left = left;
        this.right = Objects.requireNonNull(right);
    }

    @SuppressWarnings("DataFlowIssue")
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
                if (bothNumeric(leftValue, rightValue, ctx.isFloating())) {
                    if (ctx.isFloating()) {
                        return new BigDecimal(leftValue).add(new BigDecimal(rightValue)).toString();
                    } else {
                        return new BigInteger(leftValue).add(new BigInteger(rightValue)).toString();
                    }
                }
                return leftValue + rightValue;
            case "-":
                rightValue = right.execute(ctx);
                if (leftValue == null) {
                    return "-" + rightValue;
                }
                assertBothNumeric(leftValue, rightValue, operator, ctx.isFloating());
                if (ctx.isFloating()) {
                    return new BigDecimal(leftValue).subtract(new BigDecimal(rightValue)).toString();
                } else {
                    return new BigInteger(leftValue).subtract(new BigInteger(rightValue)).toString();
                }
            case "*":
                rightValue = right.execute(ctx);
                assertBothNumeric(leftValue, rightValue, operator, ctx.isFloating());
                if (ctx.isFloating()) {
                    return new BigDecimal(leftValue).multiply(new BigDecimal(rightValue)).toString();
                } else {
                    return new BigInteger(leftValue).multiply(new BigInteger(rightValue)).toString();
                }
            case "/":
                rightValue = right.execute(ctx);
                assertBothNumeric(leftValue, rightValue, operator, ctx.isFloating());
                if (ctx.isFloating()) {
                    return new BigDecimal(leftValue).divide(new BigDecimal(rightValue), ctx.getScale(), ctx.getRoundingMode()).toString();
                } else {
                    return new BigInteger(leftValue).divide(new BigInteger(rightValue)).toString();
                }
            case "%":
                rightValue = right.execute(ctx);
                assertBothNumeric(leftValue, rightValue, operator, ctx.isFloating());
                if (ctx.isFloating()) {
                    return new BigDecimal(leftValue).remainder(new BigDecimal(rightValue)).toString();
                } else {
                    return new BigInteger(leftValue).mod(new BigInteger(rightValue)).toString();
                }
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
                return String.valueOf(!isTrue(right.execute(ctx)));
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
        if (bothNumeric(leftValue, b, ctx.isFloating())) {
            return String.valueOf(predicate.test(getCompareTo(leftValue, b, ctx.isFloating())));
        }
        return String.valueOf(predicate.test(leftValue.compareTo(b)));
    }

    private static void assertBothNumeric(final String leftValue, final String rightValue, final String operator, final boolean floating) throws BadSyntax {
        assertNotNull(leftValue, operator);
        if (!bothNumeric(leftValue, rightValue, floating)) {
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

    static boolean bothNumeric(final String leftValue, final String rightValue, boolean floating) {
        if (floating)
            return leftValue != null && isFloatingPointNumber(leftValue) && rightValue != null && isFloatingPointNumber(rightValue);
        else
            return leftValue != null && InputHandler.isNumber(leftValue) && rightValue != null && InputHandler.isNumber(rightValue);
    }

    private static final Pattern BIG_DECIMAL_PATTERN = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$");

    private static boolean isFloatingPointNumber(String str) {
        return !str.isEmpty() && BIG_DECIMAL_PATTERN.matcher(str).matches();
    }

    private static int getCompareTo(final String leftValue, final String rightValue, final boolean floating) {
        if (floating) {
            return new BigDecimal(leftValue).compareTo(new BigDecimal(rightValue));
        } else {
            return new BigInteger(leftValue).compareTo(new BigInteger(rightValue));
        }
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
        return !test.trim().isEmpty();
    }

}
