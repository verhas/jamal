package javax0.jamal.snippet.tools;

public class Pluralizer {

    public static String pluralize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        String lowerCaseWord = word.toLowerCase();
        int length = word.length();

        // Ending with 'y' (but not 'ay', 'ey', 'iy', 'oy', 'uy')
        if (lowerCaseWord.endsWith("y") && length > 1 && !"aeiou".contains(Character.toString(lowerCaseWord.charAt(length - 2)))) {
            if (word.endsWith("y")) {
                return word.substring(0, length - 1) + "ies";
            } else {
                return word.substring(0, length - 1) + "IES";
            }
        }

        // Ending with 's', 'sh', 'ch', 'x' or 'z'
        if (lowerCaseWord.endsWith("s") || lowerCaseWord.endsWith("sh") || lowerCaseWord.endsWith("ch") || lowerCaseWord.endsWith("x") || lowerCaseWord.endsWith("z")) {
            if (word.endsWith("S") || word.endsWith("SH") || word.endsWith("CH") || word.endsWith("X") || word.endsWith("Z")) {
                return word + "ES";
            }
            if (word.endsWith("sH") || word.endsWith("cH")) {
                return word + "eS";
            }
            if (word.endsWith("Sh") || word.endsWith("Ch")) {
                return word + "Es";
            }
            return word + "es";
        }

        // Regular plural (just add 's')
        if (Character.isUpperCase(word.charAt(word.length()-1))) {
            return word + "S";
        } else {
            return word + "s";
        }
    }

}
