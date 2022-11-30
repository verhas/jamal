package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Processor;

import java.util.IllegalFormatException;

import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;

public class Counter implements Identified, Evaluable {
    final String id;
    int value;
    int lastValue;
    final int step;
    final String format;
    final Processor processor;

    public Counter(String id, int start, int step, String format, final Processor processor) {
        this.id = id;
        this.step = step;
        this.value = start;
        this.lastValue = start;
        this.format = format;
        this.processor = processor;
    }

    private static final String alphabet = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public String evaluate(String... parameters) throws BadSyntax {
        final String s;
        if (parameters.length > 0) {
            if (parameters[0].equals("last")) {
                s = formatValue(lastValue);
            } else if (parameters[0].length() > 0 && parameters[0].charAt(0) == '>') {
                s = getAndIncrease();
                final var id = parameters[0].substring(1).trim();
                final var macro = processor.newUserDefinedMacro(convertGlobal(id), s, true, new String[0]);
                if (isGlobalMacro(id)) {
                    processor.defineGlobal(macro);
                } else {
                    processor.define(macro);
                }
                processor.getRegister().export(id);
            } else {
                s = getAndIncrease();
            }
        } else {
            s = getAndIncrease();
        }
        return s;
    }

    private String getAndIncrease() throws BadSyntax {
        final String s;
        s = formatValue(value);
        lastValue = value;
        value += step;
        return s;
    }

    private String formatValue(int value) throws BadSyntax {
        String s;
        try {
            s = String.format(format, value);
        } catch (IllegalFormatException e) {
            throw new BadSyntax("The format string in macro '" + getId() + "' is incorrect.", e);
        }
        if (s.contains("$alpha") || s.contains("$ALPHA")) {
            BadSyntax.when(value < 1 || value > alphabet.length(),  "Counter '%s' grew too big to be formatted as a letter", id);

            s = s.replace("$alpha", alphabet.substring(value - 1, value))
                    .replace("$ALPHA", ALPHABET.substring(value - 1, value));

        }
        if (s.contains("$roman") || s.contains(("$ROMAN"))) {
            final var roman = toRoman(value, id);
            s = s.replace("$ROMAN", roman)
                    .replace("$roman", roman.toLowerCase());
        }
        return s;
    }

    private static String toRoman(int value, String id) throws BadSyntax {
        BadSyntax.when(value < 1 || value > 3999,  "Counter '%s' grew too big to be formatted as a roman numeral", id);
        StringBuilder s = new StringBuilder();
        while (value >= 1000) {
            s.append('M');
            value -= 1000;
        }
        while (value >= 900) {
            s.append("CM");
            value -= 900;
        }
        while (value >= 500) {
            s.append('D');
            value -= 500;
        }
        while (value >= 400) {
            s.append("CD");
            value -= 400;
        }
        while (value >= 100) {
            s.append('C');
            value -= 100;
        }
        while (value >= 90) {
            s.append("XC");
            value -= 90;
        }
        while (value >= 50) {
            s.append('L');
            value -= 50;
        }
        while (value >= 40) {
            s.append("XL");
            value -= 40;
        }
        while (value >= 10) {
            s.append('X');
            value -= 10;
        }
        while (value >= 9) {
            s.append("IX");
            value -= 9;
        }
        while (value >= 5) {
            s.append('V');
            value -= 5;
        }
        while (value >= 4) {
            s.append("IV");
            value -= 4;
        }
        while (value >= 1) {
            s.append('I');
            value -= 1;
        }
        return s.toString();
    }

    @Override
    public int expectedNumberOfArguments() {
        return 0;
    }

    @Override
    public String getId() {
        return id;
    }
}
