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

    final boolean iiii;

    public Counter(final String id, final int start, final int step, final String format, final boolean iiii, final Processor processor) {
        this.id = id;
        this.step = step;
        this.value = start;
        this.lastValue = start;
        this.format = format;
        this.processor = processor;
        this.iiii= iiii;
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
                final var macro = processor.newUserDefinedMacro(convertGlobal(id), s, true);
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
            BadSyntax.when(value < 1 || value > alphabet.length(), "Counter '%s' grew too big to be formatted as a letter", id);

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

    private static final int[] ROMANS = {1, 'I', 5, 'V', 10, 'X', 50, 'L', 100, 'C', 500, 'D', 1000, 'M'};

    /**
     * Haec methodus datam rationem ad numeros Romanos convertit. Modulus "id" solum nuntium errorem componere pro casu
     * cum numerus affirmativus vel nimius non est.
     *
     * @param value ad valorem convertendi
     * @param id nomen contra
     * @return Romano numero quasi filum
     * @throws BadSyntax quando numerus non convenit
     */
    private String toRoman(int value, String id) throws BadSyntax {
        if( value == 4 && iiii ){
            return "IIII";
        }
        BadSyntax.when(value < 1 || value > 3999, "Counter '%s' grew too big to be formatted as a roman numeral", id);
        StringBuilder s = new StringBuilder();
        int i = ROMANS.length - 2;
        while (i >= 0) {
            while (value >= ROMANS[i]) {
                s.append((char) ROMANS[i + 1]);
                value -= ROMANS[i];
            }
            final var k = i % 4 == 0 ? 4 : 2;
            if (i >= k && value >= ROMANS[i] - ROMANS[i - k]) {
                s.append((char) ROMANS[i - k + 1]);
                s.append((char) ROMANS[i + 1]);
                value -= ROMANS[i] - ROMANS[i - k];
            }
            i -= 2;
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
