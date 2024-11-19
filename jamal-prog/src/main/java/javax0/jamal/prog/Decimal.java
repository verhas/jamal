package javax0.jamal.prog;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.BiFunction;

public class Decimal implements Macro, Scanner {

    public static class Accumulator {
        private BigDecimal accumulator;
        private final int scale;

        private Accumulator(final BigDecimal init, int scale) {
            accumulator = init.setScale(scale, RoundingMode.HALF_UP);
            this.scale = scale;
        }

        @Override
        public String toString() {
            return accumulator.toString();
        }
    }

    private static abstract class Op implements Identified, Evaluable {
        private final String id;
        private final BiFunction<BigDecimal, BigDecimal, BigDecimal> operator;
        private final Accumulator accumulator;

        private Op(String id, BiFunction<BigDecimal, BigDecimal, BigDecimal> operator, Accumulator accumulator) {
            this.id = id;
            this.operator = operator;
            this.accumulator = accumulator;
        }

        @Override
        public String evaluate(String... parameters) throws BadSyntax {
            for (final var parameter : parameters) {
                final var value = new BigDecimal(parameter);
                accumulator.accumulator = operator.apply(accumulator.accumulator, value);
            }
            return accumulator.toString();
        }

        @Override
        public String getId() {
            return id;
        }
    }

    public static class DecimalValue extends Op {

        private DecimalValue(String id, Accumulator init) {
            super(id, BigDecimal::add, init);
        }

        public String evaluate(String... parameters) throws BadSyntax {
            if (parameters.length != 0) {
                throw new BadSyntax("Decimal value cannot have parameters");
            }
            return super.accumulator.toString();
        }
    }

    public static class Add extends Op {

        private Add(String id, Accumulator init) {
            super(id + ":add", BigDecimal::add, init);
        }
    }

    public static class Sub extends Op {

        private Sub(String id, Accumulator init) {
            super(id + ":sub", BigDecimal::subtract, init);
        }
    }

    public static class Mul extends Op {

        private Mul(String id, Accumulator init) {
            super(id + ":mul", BigDecimal::multiply, init);
        }
    }

    public static class Div extends Op {

        private Div(String id, Accumulator init) {
            super(id + ":div", BigDecimal::divide, init);
        }

        public String evaluate(String... parameters) throws BadSyntax {
            for (final var parameter : parameters) {
                var value = new BigDecimal(parameter).setScale(super.accumulator.scale, RoundingMode.HALF_UP);
                super.accumulator.accumulator = super.accumulator.accumulator.divide(value, MathContext.DECIMAL128);
            }
            return super.accumulator.toString();
        }

    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var scale = scanner.number("scale").defaultValue(2);
        scanner.done();
        InputHandler.skipWhiteSpaces(in);
        final var id = InputHandler.fetchId(in);
        InputHandler.skipWhiteSpaces(in);
        if (!in.toString().startsWith("=")) {
            throw new BadSyntax("Missing '=' after decimal definition");
        }
        InputHandler.skip(in, 1);
        InputHandler.skipWhiteSpaces(in);
        final Accumulator init = new Accumulator(new BigDecimal(in.toString()), scale.get());
        final var add = new Add(id, init);
        final var val = new DecimalValue(id, init);
        final var sub = new Sub(id, init);
        final var mul = new Mul(id, init);
        final var div = new Div(id, init);
        processor.defineGlobal(add);
        processor.defineGlobal(val);
        processor.defineGlobal(sub);
        processor.defineGlobal(mul);
        processor.defineGlobal(div);
        return "";
    }
}
