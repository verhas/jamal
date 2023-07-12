package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Bir implements Macro, Scanner {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var delimiters = scanner.str("bir$delimiters", "delimiters").defaultValue(":~:~:_:_:[:]");
        final var prefix = scanner.str("bir$prefix", "prefix").defaultValue("**");
        final var postfix = scanner.str("bir$postfix", "postfix").defaultValue("**");
        final var ratios = scanner.str("bir$ratios", "ratios").defaultValue("- 0 1 1 2 0.4");
        final var dictionary = scanner.str(BirDictionary.DEFAULT_DICTIONARY_NAME, "dictionary", "dict")
                .defaultValue(BirDictionary.DEFAULT_DICTIONARY_NAME);
        final var pfDictionary = scanner.str(BirDictionary.DEFAULT_PF_DICTIONARY_NAME, "ending", "endings", "pf")
                .defaultValue(BirDictionary.DEFAULT_PF_DICTIONARY_NAME);
        final var cmDictionary = scanner.str(BirDictionary.DEFAULT_CM_DICTIONARY_NAME, "common", "commons", "cm")
                .defaultValue(BirDictionary.DEFAULT_CM_DICTIONARY_NAME);
        scanner.done();

        BadSyntax.when(ratios.get().length() < 11,
                "The parameter \"ratios\" is malformed, it must look like '- 0 1 1 2 0.4', at least 11 characters");
        final var p = ratios.get().charAt(0);
        BadSyntax.when(p != '+' && p != '-',
                "The parameter \"ratios\" is malformed, it must look like '- 0 1 1 2 0.4', the first character must be '+' or '-'");

        final var input = in.toString();
        if (input.length() == 0) {
            return "";
        }

        final var dictionaryName = dictionary.get();
        final var dict = getBirDictonary(processor, dictionaryName, BirDictionary.DEFAULT_DICTIONARY_NAME);
        final var pfDict = getBirDictonary(processor, pfDictionary.get(), BirDictionary.DEFAULT_PF_DICTIONARY_NAME);
        final var cmDict = getBirDictonary(processor, cmDictionary.get(), BirDictionary.DEFAULT_CM_DICTIONARY_NAME);

        final var rationes = calculateRationes(ratios.get());
        final var birifyCommonWords = p == '+';


        final var words = splitToWords(input);
        final String[] dPairs = getDelimiters(delimiters);
        final var birifier = new Birifier(prefix.get(), postfix.get(), rationes, birifyCommonWords, dict, pfDict, cmDict);
        for (int i = 0; i < words.length; i++) {
            if (!isSkip(words, i, dPairs)) {
                words[i] = birifier.birify(words[i]);
            }
        }

        return String.join("", words);
    }

    /**
     * Retrieves a dictionary from a macro based on the specified dictionary name.
     * The macro may not exist, which is not an error when the name is the default name.
     * In other cases, when the name comes from explicit parameter, then the dictionary must exist.
     * If it does not, then the method throws BadSyntax exception.
     *
     * @param processor      The Processor object from which to retrieve the dictionary.
     * @param dictionaryName The name of the dictionary to retrieve.
     * @param defaultName    The default name to compare against the dictionary name.
     * @return The BirDictionary object corresponding to the given dictionary name,
     * or null if the dictionary name is null or the dictionary is not defined.
     * @throws BadSyntax If the dictionary name is not equal to the default name and the dictionary is not defined.
     */
    private static BirDictionary.BirDictonary getBirDictonary(Processor processor,
                                                              String dictionaryName,
                                                              final String defaultName) throws BadSyntax {
        if (dictionaryName == null) {
            return null;
        } else {
            final var dict = (BirDictionary.BirDictonary) processor
                    .getRegister()
                    .getUserDefined(dictionaryName)
                    .filter(m -> m instanceof BirDictionary.BirDictonary).orElse(null);
            BadSyntax.when(!defaultName.equals(dictionaryName) && dict == null,
                    "The dictionary macro \"%s\" is not defined", dictionaryName);

            return dict;
        }
    }


    /**
     * Calculates an array of ratios based on the provided string.
     * The string contains ratio values separated by spaces.
     * The first character of the string is either '+' or '-', but this is ignored and skipped in this method.
     * The number of ratios is limited by the constant Birifier.RATIONES.
     * The first Biri.RATIONES - 1 ratios are integers meaning the number of characters, the last one is a double value.
     * The last value means the number of characters to be bolded at the start of the word when the number of the
     * characters is more than Biri.RATIONES - 1.
     * <p>
     * The typical value of the parameter is "- 0 1 1 2 0.4".
     * <p>
     * This formatting is used by the Chrome extension Bionify, https://github.com/Cveinnt/bionify
     * I do not think the format itself is intellectual property, and it is nice to be somewhat compatible.
     *
     * @param rs The string containing the ratios.
     * @return An array of integers representing the calculated ratios.
     * @throws BadSyntax If there are too many ratios in the parameter, if a ratio is invalid,
     *                   or if the number of ratios exceeds the limit specified by Birifier.RATIONES.
     */
    private static int[] calculateRationes(String rs) throws BadSyntax {
        int[] rationes = new int[Birifier.RATIONES];
        int i = 0;
        for (final var s : rs.substring(1).split("\\s+")) {
            if (0 != s.length()) {
                BadSyntax.when(i == Birifier.RATIONES, "Too many ratios in the parameter \"%%s\"%s", rs);
                if (i < Birifier.RATIONES - 1) {
                    rationes[i] = toInt(rs, s, Integer::parseInt);
                    BadSyntax.when(rationes[i] < 0 || rationes[i] > i + 1,
                            "Invalid number %s at the position %d in the parameter \"%s\"", s, i + 1, rs);
                } else {
                    rationes[i] = toInt(rs, s, k -> (int) (Double.parseDouble(k) * 100));
                    BadSyntax.when(rationes[i] < 0 || rationes[i] > 100,
                            "Invalid number %s at the position %d in the parameter \"%s\"", s, i + 1, rs);
                }
                i++;
            }
        }
        return rationes;
    }

    /**
     * Converts a string representation of a number to an integer using the provided function.
     *
     * @param ratios The original string containing the ratios.
     * @param s      The string representation of a number to convert.
     * @param f      The function used for the conversion.
     * @return The converted integer value.
     * @throws BadSyntax If the number is invalid and cannot be converted to an integer.
     */
    private static int toInt(String ratios, String s, Function<String, Integer> f) throws BadSyntax {
        try {
            return f.apply(s);
        } catch (NumberFormatException nfe) {
            throw new BadSyntax(String.format("Invalid number '%s' in the parameter '%s'", s, ratios), nfe);
        }
    }

    /**
     * Retrieves an array of delimiters from the provided parameter.
     * Delimiters are string pairs that prevent the birification of words between them.
     * If the parameter is an empty string, an empty array is returned.
     * Otherwise, the parameter is split into pairs of delimiters using the first character as a separator.
     *
     * @param delimiters The parameter containing the delimiters.
     * @return An array of strings representing the delimiters.
     * @throws BadSyntax If an error occurs while processing the delimiters.
     */
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

    /**
     * Checks if a word at a specific index should be skipped based on the provided word array and dependency pairs.
     * <p>
     * A word is skipped and not birified when the
     *
     * <ul>
     *     <li>word is not a letter,</li>
     *     <li>word is the first or the last word in the array,</li>
     *     <li>word is between two non-words that form a delimiter pair</li>
     * </ul>
     *
     * @param words  The array of words.
     * @param i      The index of the word to check.
     * @param dPairs The array of delimiter pairs.
     * @return {@code true} if the word should be skipped, {@code false} otherwise.
     */
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

    /**
     * Splits the input string into individual words and returns them as an array of strings.
     * A word is defined as a consecutive sequence of alphabetic characters.
     * <p>
     * The returned array contains the words and the separating non-word strings in the original order.
     * For example, the input string "Hello, world!" is split into the array ["Hello", ", ", "world", "!"].
     *
     * @param input The string to be split into words.
     * @return An array of strings representing the individual words and noon-words in the input string.
     */
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
