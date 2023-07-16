package javax0.jamal.snippet;

import javax0.jamal.api.Identified;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

class Decorator {

    final List<Function<String, String>> decorators;
    final int[] charNums;
    final int ratio;
    final boolean decorateCommonWords;
    final boolean repeat;
    final Dictionary dict;

    private final String[] postfixes;
    final int minPfLength;
    private static final String[] POSTFIXES = {
            // snippet DEFAULT_POSTFIXES
            "ition", "ement", "ssion", "ional", "ction", "ation", "sion", "ning",
            "ther", "ight", "tion", "ture", "ding", "tive", "ally", "rate",
            "ting", "ance", "ence", "ment", "lity", "ical", "able", "onal",
            "tly", "ose", "ast", "ess", "use", "est", "ion", "ice",
            "ist", "hip", "tic", "der", "ate", "her", "ect", "nal",
            "ite", "ral", "ter", "all", "ver", "ide", "ity", "ght",
            "ely", "ain", "ous", "cal", "nce", "ial", "are", "low",
            "tor", "and", "ear", "ian", "ive", "eat", "ere", "ble",
            "end", "ire", "ine", "ual", "ing", "ore", "ant", "one",
            "ure", "ary", "ent", "ase", "lly", "ise", "age", "ish"
            // end snippet
    };
    final Set<String> commonWords;
    final static Set<String> COMMONG_WORDS = Set.of(
            // snippet DEFAULT_COMMON_WORDS
            "the", "be", "to", "of", "and", "a", "an", "it", "at", "on", "he", "she", "but", "is", "my"
            // end snippet
    );

    /**
     * Constructs a new Decorator object with the specified parameters.
     *
     * @param decorators          is the array of the decorators to be used. If a word is split into more parts than the
     *                            number of decorators, then identity functions will be used for the missing decorators.
     * @param charNums            array of character positions used for splitting. The first element is the position for
     *                            splitting for one character words. The second element is the position for splitting
     *                            for two character words and so on. If the array is shorter than the number of
     *                            characters in the word, then the parameter repeat will be used, interpreted as a
     *                            percentage value.
     * @param repeat              the percentage of characters to be used for splitting if the array charNums is shorter
     *                            than the actual word.
     * @param decorateCommonWords flag if the common words should be decorated.
     * @param dict
     * @param postDict
     * @param commonDict
     */
    Decorator(List<Function<String, String>> decorators,
              int[] charNums,
              int ratio,
              boolean repeat,
              boolean decorateCommonWords,
              Dictionary dict,
              Dictionary postDict,
              Dictionary commonDict
    ) {
        this.repeat = repeat;
        this.decorators = decorators;
        this.charNums = charNums;
        this.ratio = ratio;
        this.decorateCommonWords = decorateCommonWords;
        this.dict = dict;
        this.commonWords = commonDict == null ? COMMONG_WORDS : commonDict.dictionary.keySet();
        this.postfixes = postDict == null ? POSTFIXES : postDict.dictionary.keySet().toArray(String[]::new);
        minPfLength = Arrays.stream(postfixes).mapToInt(String::length).min().orElse(MIN_PF_LENGTH);
    }

    private static final int MIN_PF_LENGTH = 3;

    /**
     * Transforms a given string into a "decorated" version based on the specified rules and parameters.
     * Decoration of a word is performed by splitting the word into several (one, two or more) parts and applying
     * different decorators to the individual parts.
     * If there are more parts than the number of decorators, then the decoration will simply be an identity.
     * In other words, the non-existent decorators will not change the string.
     * <p>
     * The splitting can be simple or recursive.
     * Simple splitting is performed by splitting the word into two parts.
     * Recursive splitting is performed by splitting the word into two parts and then splitting the second part again
     * using the same algorithm.
     * <p>
     * If a dictionary is available and the current word is included in the dictionary along with a character position,
     * the word will first be split into two parts at the specified position.
     * <p>
     * If the string is a common word and decoration of common words is enabled, it applies decoration to the whole word.
     * <p>
     * If there is a postfix dictionary, it checks whether the string ends with a postfix.
     * If a postfix is found, it adjusts the length of the first section so that no letter from the postfix will be part
     * of the first section.
     * <p>
     * If the length of the string is small, the length of the first section will be given by the number of the
     * characters specified.
     * If the length was already less than calculated because of the postfixes, then the length will not be increased.
     * <p>
     * If the string is long, then the last number from the array 'rationes' will be used as a percentage value.
     * <p>
     * If no decorated section is determined, it returns the original string.
     *
     * @param s     The input string to be decorated.
     * @param start The index of the first decorator to be applied.
     * @return The decorated version of the input string.
     */

    String decorate(final String s, int start) {
        final Function<String, String> firstDecorator = getFirstDecorator(start);
        final Function<String, String> restDecorator = getSecondDecorator(start);

        if (restDecorator == null) {
            return firstDecorator.apply(s);
        }
        if (dict != null) {
            final var b = dict.get(s);
            if (b != -1) {
                return b == 0 ? s : firstDecorator.apply(s.substring(0, b)) + restDecorator.apply(s.substring(b));
            }
        }
        if (commonWords.contains(s)) {
            return decorateCommonWords ? firstDecorator.apply(s) : s;
        }
        int prefixLength = s.length();
        if (prefixLength > minPfLength) {
            for (final var p : postfixes) {
                if (s.endsWith(p)) {
                    prefixLength = s.length() - p.length();
                    break;
                }
            }
        }
        if (s.length() < charNums.length - 1) {
            prefixLength = Math.min(prefixLength, charNums[s.length() - 1]);
        } else {
            prefixLength = Math.min(prefixLength, ratio * s.length() / 100);
        }
        if (prefixLength > 0) {
            return firstDecorator.apply(s.substring(0, prefixLength)) + restDecorator.apply(s.substring(prefixLength));
        } else {
            return s;
        }
    }

    private Function<String, String> getFirstDecorator(int start) {
        final Function<String, String> dOne;
        if (decorators.size() <= start) {
            dOne = x -> x;
        } else {
            dOne = decorators.get(start);
        }
        return dOne;
    }

    private Function<String, String> getSecondDecorator(int start) {
        final Function<String, String> dTwo;
        if (repeat) {
            dTwo = x -> decorate(x, start + 1);
        } else {
            if (decorators.size() <= start + 1) {
                dTwo = null;
            } else {
                dTwo = decorators.get(start + 1);
            }
        }
        return dTwo;
    }

    /**
     * Represents a dictionary used for decoration, associating strings with their corresponding integer values.
     * The dictionary allows retrieval of the integer value associated with a given string.
     */
    static class Dictionary implements Identified {
        private final String name;
        final Map<String, Integer> dictionary;

        /**
         * Retrieves the integer value associated with the specified key in the dictionary.
         *
         * @param key The key for which to retrieve the associated integer value.
         * @return The integer value associated with the key, or -1 if the key is not found in the dictionary.
         */
        int get(String key) {
            if (dictionary.containsKey(key)) {
                return dictionary.get(key);
            }
            return -1;
        }

        /**
         * Constructs a new BirDictionary with the specified name and dictionary map.
         *
         * @param name       The name of the dictionary.
         * @param dictionary The map representing the dictionary, associating strings with integer values.
         */
        Dictionary(String name, Map<String, Integer> dictionary) {
            this.name = name;
            this.dictionary = dictionary;
        }

        @Override
        public String getId() {
            return name;
        }
    }
}
