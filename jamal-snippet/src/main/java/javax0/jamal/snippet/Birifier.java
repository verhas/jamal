package javax0.jamal.snippet;

import java.util.Set;

class Birifier {

    final String prefix;
    final String postfix;
    final int[] rationes;
    final boolean birifyCommonWords;
    final BirDictionary.BirDictonary dict;

    private final String[] postfixes;
    private static final String[] POSTFIXES = {
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
    final Set<String> commonWords;
    final static Set<String> COMMONG_WORDS = Set.of(
            "the", "be", "to", "of", "and", "a", "an", "it", "at", "on", "he", "she", "but", "is", "my"
    );

    Birifier(String prefix,
             String postfix,
             int[] rationes,
             boolean birifyCommonWords,
             BirDictionary.BirDictonary dict,
             BirDictionary.BirDictonary postDict,
             BirDictionary.BirDictonary commonDict
    ) {
        this.prefix = prefix;
        this.postfix = postfix;
        this.rationes = rationes;
        this.birifyCommonWords = birifyCommonWords;
        this.dict = dict;
        this.commonWords = commonDict == null ? COMMONG_WORDS : commonDict.dictionary.keySet();
        this.postfixes = postDict == null ? POSTFIXES : postDict.dictionary.keySet().toArray(String[]::new);
    }

    static final int RATIONES = 5;
    private static final int MAX_PF_LENGTH = 5;

    /**
     * Transforms a given string into a "birified" version based on the specified rules and parameters.
     * <p>
     * Birification is to transform a string into a version that is easier to read and understand highlighting a few
     * characters at the beginning of the string.
     * <p>
     * If a dictionary is available, it attempts to retrieve a modified version of the string from the dictionary.
     * If a modified version is found, it applies prefix and postfix to the corresponding sections of the string.
     * If the string is a common word and birification of common words is enabled, it applies prefix and postfix to the whole word.
     * If the length of the string exceeds the maximum prefix length, it checks if the string ends with any of the defined postfixes.
     * If a postfix is found, it adjusts the length of the birred section accordingly.
     * If the length of the string is within the predefined ratios, it adjusts the length of the birred section accordingly.
     * If the length of the string exceeds the predefined ratios, it adjusts the length of the birred section proportionally.
     * Finally, it constructs the birified string by combining the prefix, birred section, and postfix.
     * If no birred section is determined, it returns the original string.
     *
     * @param s The input string to be birified.
     * @return The birified version of the input string.
     */

    String birify(final String s) {
        if (dict != null) {
            final var b = dict.get(s);
            if (b != -1) {
                return b == 0 ? s : prefix + s.substring(0, b) + postfix + s.substring(b);
            }
        }
        if (commonWords.contains(s)) {
            return birifyCommonWords ? prefix + s + postfix : s;
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
}
