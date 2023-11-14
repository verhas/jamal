package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class MathMacros {

    private static double parse(String s) {
        try {
            if (s.startsWith("0x") || s.startsWith("0X")) {
                return (double) Long.parseLong(s.substring(2), 16);
            }
            return Double.parseDouble(s);
        } catch (NumberFormatException nfe) {
            throw BadSyntax.rt("The string '" + s + "' cannot be interpreted as a number");
        }
    }

    private static String toStringNice(double d) {
        final var s = "" + d;
        if (s.endsWith(".0")) {
            return s.substring(0, s.length() - 2);
        }
        return s;
    }

    private static double[] getOperands(Input in) throws BadSyntaxAt {
        try {
            return Arrays.stream(in.toString().split("\\s+")).mapToDouble(MathMacros::parse).toArray();
        } catch (RuntimeException re) {
            throw new BadSyntaxAt((BadSyntax) re.getCause(), in.getPosition());
        }
    }

    public static class Plus implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            double[] ops = getOperands(in);
            double acc = 0.0;
            for (final var op : ops) {
                acc += op;
            }
            return toStringNice(acc);
        }
    }

    public static class Mult implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            double[] ops = getOperands(in);
            double acc = 1.0;
            for (final var op : ops) {
                acc *= op;
            }
            return toStringNice(acc);
        }
    }

    public static class Sub implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            double[] ops = getOperands(in);
            double acc = ops[0];
            for (int i = 1; i < ops.length; i++) {
                acc -= ops[i];
            }
            return toStringNice(acc);
        }
    }

    public static class Div implements Macro {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            double[] ops = getOperands(in);
            double acc = ops[0];
            for (int i = 1; i < ops.length; i++) {
                acc -= ops[i];
            }
            return toStringNice(acc);
        }
    }

    public static class Eq implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            double[] ops = getOperands(in);
            double acc = ops[0];
            for (int i = 1; i < ops.length; i++) {
                if (acc != ops[i]) {
                    return "false";
                }
            }
            return "true";
        }
    }

    public static class LessThan implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            double[] ops = getOperands(in);
            double acc = ops[0];
            for (int i = 1; i < ops.length; i++) {
                if (acc >= ops[i]) {
                    return "false";
                }
                acc = ops[i];
            }
            return "true";
        }
    }

    public static class Le implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            double[] ops = getOperands(in);
            double acc = ops[0];
            for (int i = 1; i < ops.length; i++) {
                if (acc > ops[i]) {
                    return "false";
                }
                acc = ops[i];
            }
            return "true";
        }
    }

    public static class Gt implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            double[] ops = getOperands(in);
            double acc = ops[0];
            for (int i = 1; i < ops.length; i++) {
                if (acc <= ops[i]) {
                    return "false";
                }
                acc = ops[i];
            }
            return "true";
        }
    }

    public static class Ge implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            double[] ops = getOperands(in);
            double acc = ops[0];
            for (int i = 1; i < ops.length; i++) {
                if (acc < ops[i]) {
                    return "false";
                }
                acc = ops[i];
            }
            return "true";
        }
    }

    public static class Min implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            double[] ops = getOperands(in);
            double acc = ops[0];
            for (int i = 1; i < ops.length; i++) {
                acc = Math.min(acc, ops[i]);
            }
            return toStringNice(acc);
        }
    }

    public static class Max implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            double[] ops = getOperands(in);
            double acc = ops[0];
            for (int i = 1; i < ops.length; i++) {
                acc = Math.max(acc, ops[i]);
            }
            return toStringNice(acc);
        }
    }

    private static final Map<String, DoubleFunction<?>> UNARIES = new HashMap<>();
    private static final Map<String, DoubleBinaryOperator> BINARIES = new HashMap<>();
    private static final Map<String, String> CONSTANTS = new HashMap<>();

    static {
        UNARIES.put("sin", Math::sin);
        UNARIES.put("cos", Math::cos);
        UNARIES.put("tan", Math::tan);
        UNARIES.put("asin", Math::asin);
        UNARIES.put("acos", Math::acos);
        UNARIES.put("atan", Math::atan);
        UNARIES.put("toRadians", Math::toRadians);
        UNARIES.put("toDegrees", Math::toDegrees);
        UNARIES.put("exp", Math::exp);
        UNARIES.put("log", Math::log);
        UNARIES.put("log10", Math::log10);
        UNARIES.put("sqrt", Math::sqrt);
        UNARIES.put("cbrt", Math::cbrt);
        UNARIES.put("ceil", Math::ceil);
        UNARIES.put("floor", Math::floor);
        UNARIES.put("rint", Math::rint);
        UNARIES.put("round", Math::round);
        UNARIES.put("abs", Math::abs);
        UNARIES.put("ulp", Math::ulp);
        UNARIES.put("signum", Math::signum);
        UNARIES.put("sinh", Math::sinh);
        UNARIES.put("cosh", Math::cosh);
        UNARIES.put("tanh", Math::tanh);
        UNARIES.put("expm1", Math::expm1);
        UNARIES.put("log1p", Math::log1p);
        UNARIES.put("getExponent", Math::getExponent);
        UNARIES.put("nextUp", Math::nextUp);
        UNARIES.put("nextDown", Math::nextDown);
        BINARIES.put("IEEEremainder", Math::IEEEremainder);
        BINARIES.put("atan2", Math::atan2);
        BINARIES.put("pow", Math::pow);
        BINARIES.put("max", Math::max);
        BINARIES.put("min", Math::min);
        BINARIES.put("hypot", Math::hypot);
        BINARIES.put("copySign", Math::copySign);
        BINARIES.put("nextAfter", Math::nextAfter);
        CONSTANTS.put("zero", "0");
        CONSTANTS.put("one", "1");
        CONSTANTS.put("two", "2");
        CONSTANTS.put("three", "3");
        CONSTANTS.put("four", "4");
        CONSTANTS.put("five", "5");
        CONSTANTS.put("six", "6");
        CONSTANTS.put("seven", "7");
        CONSTANTS.put("eight", "8");
        CONSTANTS.put("nine", "9");
        CONSTANTS.put("ten", "10");
        CONSTANTS.put("eleven", "11");
        CONSTANTS.put("twelve", "12");
        CONSTANTS.put("half", "0.5");
        CONSTANTS.put("pi", "3.14159 26535 89793 23846");
        CONSTANTS.put("e", "2.71828 18284 59045 23536");
        CONSTANTS.put("phi", "1.61803 39887 49894 84820");
    }

    public static class Const implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var constantName = fetchId(in);
            BadSyntaxAt.when(!CONSTANTS.containsKey(constantName),"The constant " + constantName,in.getPosition());
            return CONSTANTS.get(constantName);
        }
    }

    public static class Fn implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var operation = fetchId(in);
            skipWhiteSpaces(in);
            double[] ops = getOperands(in);
            if (UNARIES.containsKey(operation)) {
                return Arrays.stream(ops)
                    .mapToObj(UNARIES.get(operation))
                    .map(s -> "" + s)
                    .collect(Collectors.joining(" "));
            }
            if (BINARIES.containsKey(operation)) {
                BadSyntaxAt.when(ops.length != 2,"Math binary operation " + operation + " needs exactly two operands, got " + ops.length,in.getPosition());

                return toStringNice(BINARIES.get(operation).applyAsDouble(ops[0], ops[1]));
            }
            if ("fma".equals(operation)) {
                BadSyntaxAt.when(ops.length != 3,"Math operation fma needs exactly three operands, got " + ops.length,in.getPosition());
                return toStringNice(Math.fma(ops[0], ops[1], ops[2]));
            }
            if ("random".equals(operation)) {
                BadSyntaxAt.when(ops.length != 0,"Math operation random does not accespt argument and got " + ops.length,in.getPosition());
                return toStringNice(Math.random());
            }
            throw new BadSyntaxAt("Math operation " + operation + " is not supported", in.getPosition());
        }
    }

}
