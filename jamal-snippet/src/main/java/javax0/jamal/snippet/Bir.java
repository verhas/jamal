package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Bir implements Macro, Scanner {

    private static final String[] postfixes = {
            "ition", "ement", "ssion", "ional", "ction", "ation", "sion", "ning",
            "ther", "ight", "tion", "ture", "ding", "tive", "ally", "rate",
            "ting", "ance", "ence", "ment", "lity", "ical", "able", "onal",
            "tly", "ose", "ast", "ess", "use", "est", "ion", "ice",
            "ist", "hip", "tic", "der", "ate", "her", "ect", "nal",
            "ite", "ral", "ter", "all", "ver", "ide", "ity", "ght",
            "ely", "ain", "ous", "cal", "nce", "ial", "are", "low",
            "tor", "and", "ear", "ian", "ive", "eat", "ere", "ble",
            "end", "ire", "ine", "ual", "ing", "ore", "ant", "one",
            "ure", "ary", "ent", "ase", "lly", "ise", "age", "ish",
    };
    final List<String> commonWords = List.of(
            "the", "be", "to", "of", "and", "a", "an", "it", "at", "on", "he", "she", "but", "is", "my"
    );

    final static int RATIONES = 5;
    private static final int MIN_PF_LENGTH = 3;
    private static final int MAX_PF_LENGTH = 5;

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var delimiters = scanner.str("bir$delimiters", "delimiters").defaultValue(":~:~:_:_:[:]");
        final var prefix = scanner.str("bir$prefix", "prefix").defaultValue("**");
        final var postfix = scanner.str("bir$postfix", "postfix").defaultValue("**");
        final var ratios = scanner.str("bir$ratios", "ratios").defaultValue("- 0 1 1 2 0.4");
        scanner.done();

        BadSyntax.when(ratios.get().length() < 11,
                "The parameter \"ratios\" is malformed, it must look like '- 0 1 1 2 0.4', at least 11 characters");
        final var p = ratios.get().charAt(0);
        BadSyntax.when(p != '+' && p != '-',
                "The parameter \"ratios\" is malformed, it must look like '- 0 1 1 2 0.4', the first character must be '+' or '-'");

        final var rationes = calculateRationes(ratios.get());
        final var birrifyCommonWords = p == '+';

        final var input = in.toString();
        if (input.length() == 0) {
            return "";
        }

        final var words = splitToWords(input);
        final String[] dPairs = getDelimiters(delimiters);

        for (int i = 0; i < words.length; i++) {
            if (!isSkip(words, i, dPairs)) {
                words[i] = birify(words[i], prefix.get(), postfix.get(), rationes, birrifyCommonWords);
            }
        }

        return String.join("", words);
    }

    private String birify(final String s, final String prefix, final String postfix, int[] rationes, final boolean birrifyCommonWords) {
        if (commonWords.contains(s)) {
            return birrifyCommonWords ? prefix + s + postfix : s;
        }
        int birred = s.length();
        if (birred > MAX_PF_LENGTH) {
            for (final var p : postfixes) {
                if (s.endsWith(p)) {
                    birred = s.length() - p.length();
                    break;
                }
            }
        }
        if (s.length() < RATIONES) {
            birred = Math.min(birred, rationes[s.length() - 1]);
        } else {
            birred = Math.min(birred, rationes[RATIONES - 1] * s.length() / 100);
        }
        if (birred > 0) {
            return prefix + s.substring(0, birred) + postfix + s.substring(birred);
        } else {
            return s;
        }
    }

    private static int[] calculateRationes(String ratios) throws BadSyntax {
        int[] rationes = new int[RATIONES];
        int i = 0;
        for (final var s : ratios.substring(1).split("\\s+")) {
            if (s.length() == 0) {
                continue;
            }
            BadSyntax.when(i == RATIONES, "Too many ratios in the parameter \"%%s\"%s", ratios);
            if (i < RATIONES - 1) {
                rationes[i] = toInt(ratios, i, s, Integer::parseInt);
                BadSyntax.when(rationes[i] < 0 || rationes[i] > i + 1,
                        "Invalid number %s at the position %d in the parameter \"%s\"", s, i + 1, ratios);
            } else {
                rationes[i] = toInt(ratios, i, s, k -> (int) (Double.parseDouble(k) * 100));
                BadSyntax.when(rationes[i] < 0 || rationes[i] > 100,
                        "Invalid number %s at the position %d in the parameter \"%s\"", s, i + 1, ratios);
            }
            i++;
        }
        return rationes;
    }

    private static int toInt(String ratios, int i, String s, Function<String, Integer> f) throws BadSyntax {
        try {
            return f.apply(s);
        } catch (NumberFormatException nfe) {
            throw new BadSyntax(String.format("Invalid number '%s' in the parameter '%s'", s, ratios), nfe);
        }
    }

    private String[] getDelimiters(Params.Param<String> delimiters) throws BadSyntax {
        final String[] dPairs;
        if (delimiters.get().length() == 0) {
            dPairs = new String[0];
        } else {
            final var sep = delimiters.get().substring(0, 1);
            dPairs = delimiters.get().substring(1).split(Pattern.quote(sep));
        }
        return dPairs;
    }

    private boolean isSkip(final String[] words, final int i, final String[] dPairs) {
        if (!Character.isLetter(words[i].charAt(0))) {
            return true;
        }
        if (i == 0 || i == words.length - 1) {
            return false;
        }
        for (int j = 0; j < dPairs.length; j += 2) {
            if (words[i - 1].endsWith(dPairs[j]) && words[i + 1].startsWith(dPairs[j + 1])) {
                return true;
            }
        }
        return false;
    }

    private static String[] splitToWords(String input) {
        final var words = new ArrayList<String>();
        int start = 0;
        int end = 0;
        boolean inWord = Character.isLetter(input.charAt(0));
        while (end < input.length()) {
            if (inWord) {
                if (Character.isLetter(input.charAt(end))) {
                    end++;
                } else {
                    inWord = false;
                    words.add(input.substring(start, end));
                    start = end;
                }
            } else {
                if (Character.isLetter(input.charAt(end))) {
                    words.add(input.substring(start, end));
                    inWord = true;
                    start = end;
                } else {
                    end++;
                }
            }
        }
        words.add(input.substring(start, end));
        return words.toArray(String[]::new);
    }

    @Override
    public String getId() {
        return "bir";
    }
}
