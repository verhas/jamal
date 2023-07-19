package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;

import java.util.IllegalFormatException;

class CounterFormatter {
    private final String format;
    private final boolean iiii;

    private final String id;

    public CounterFormatter(String format, boolean iiii, String id) {
        this.format = format;
        this.iiii = iiii;
        this.id = id;
    }

    private static final String alphabet = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String greek = "\u03B1\u03B2\u03B3\u03B4\u03B5\u03B6\u03B7\u03B8\u03B9\u03BA\u03BB" +
            "\u03BC\u03BD\u03BE\u03BF\u03C0\u03C1\u03C3\u03C4\u03C5\u03C6\u03C7\u03C8\u03C9";
    private static final String GREEK = "\u0391\u0392\u0393\u0394\u0395\u0396\u0397\u0398\u0399\u039A\u039B" +
            "\u039C\u039D\u039E\u039F\u03A0\u03A1\u03A3\u03A4\u03A5\u03A6\u03A7\u03A8\u03A9";

    private static final String cyrillic = "\u0430\u0431\u0432\u0433\u0434\u0435\u0436\u0437\u0438\u0439\u043A\u043B\u043C\u043D\u043E" +
            "\u043F\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447\u0448\u0449\u044A\u044B\u044C\u044D\u044E\u044F";
    private static final String CYRILLIC = "\u0410\u0411\u0412\u0413\u0414\u0415\u0416\u0417\u0418\u0419\u041A\u041B\u041C\u041D\u041E" +
            "\u041F\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427\u0428\u0429\u042A\u042B\u042C\u042D\u042E\u042F";

    String formatValue(int value) throws BadSyntax {
        return formatValue(value, null);
    }

    String formatValue(int value, String title) throws BadSyntax {
        String s;
        try {
            s = String.format(format, value);
        } catch (IllegalFormatException e) {
            throw new BadSyntax("The format string for counter '" + id + "' is incorrect.", e);
        }
        if (title != null && (s.contains("$title") || s.contains("$TITLE"))) {
            s = s.replace("$title", title)
                    .replace("$TITLE", title);
        }

        s = replaceAlpha(s, "alpha", alphabet, ALPHABET, value);
        s = replaceAlpha(s, "latin", alphabet, ALPHABET, value);
        s = replaceAlpha(s, "greek", greek, GREEK, value);
        s = replaceAlpha(s, "cyrillic", cyrillic, CYRILLIC, value);

        if (s.contains("$roman") || s.contains(("$ROMAN"))) {
            final var roman = toRoman(value, id);
            s = s.replace("$ROMAN", roman)
                    .replace("$roman", roman.toLowerCase());
        }
        return s;
    }

    private String replaceAlpha(final String s,
                                final String name,
                                final String lower,
                                final String UPPER,
                                final int value) throws BadSyntax {
        final var NAME = name.toUpperCase();
        if (s.contains("$" + name) || s.contains("$" + NAME)) {
            BadSyntax.when(value < 1 || value > lower.length(), "Counter '%s' grew too big to be formatted as a %s letter", name, id);

            return s.replace("$" + name, lower.substring(value - 1, value))
                    .replace("$" + NAME, UPPER.substring(value - 1, value));
        }else {
            return s;
        }
    }

    private static final int[] ROMANS = {1, 'I', 5, 'V', 10, 'X', 50, 'L', 100, 'C', 500, 'D', 1000, 'M'};

    /**
     * Haec methodus datam rationem ad numeros Romanos convertit. Modulus "id" solum nuntium errorem componere pro casu
     * cum numerus affirmativus vel nimius non est.
     *
     * @param value ad valorem convertendi
     * @param id    nomen contra
     * @return Romano numero quasi filum
     * @throws BadSyntax quando numerus non convenit
     */
    private String toRoman(int value, String id) throws BadSyntax {
        if (value == 4 && iiii) {
            return "IIII";
        }
        BadSyntax.when(value < 1 || value > 3999, "Numerus '%s' magnus est, ut uti numeralibus Romanis formatur impossible.", id);
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

}
