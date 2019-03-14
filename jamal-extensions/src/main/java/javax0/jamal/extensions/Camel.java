package javax0.jamal.extensions;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

/**
 * Macros defined in static inner classes that some way camel case the input.
 */
public class Camel {

    /**
     * Convert an camel cased word int a sentence, for example:
     * {@code thisIsACamelCaseWord -> "this is a camel case word"}
     *
     * @param s the camel cased word
     * @return the sentence
     */
    private static String sentence(String s) {
        var c = new StringBuilder();
        var first = true;
        for (var ch : s.toCharArray()) {
            if (Character.isUpperCase(ch) && !first) {
                c.append(" ");
            }
            c.append(Character.toLowerCase(ch));
            first = false;
        }
        return c.toString();
    }

    /**
     * Converts a camel cased word into a C style all upper case variable name. For example
     * {@code thisIsACamelCaseWord -> THIS_IS_A_CAMEL_CASE_WORLD} (and the code works even better
     * because it does not make typing mistakes.
     *
     * @param s the camel cased word
     * @param sep the separator between the words. In the example it is underscore
     * @return the C style converted word
     */
    private static String cstyle(String s, char sep) {
        var c = new StringBuilder();
        var first = true;
        for (var ch : s.toCharArray()) {
            if (Character.isUpperCase(ch) && !first) {
                c.append(sep);
            }
            c.append(Character.toUpperCase(ch));
            first = false;
        }
        return c.toString();
    }

    /**
     * Convert a sentence to a camel cased word. For example
     * {@code  This IS a CaMel case word. -> thisIsACamelCaseWord}
     * Note  that all characters except alpha and digits are removed. result starts with lower case letter and
     * uppercase only those that follow a deleted character in the original sentence.
     * @param s the sentence to be converted
     * @return the converted word
     */
    private static String camelCase(String s) {
        var cased = new StringBuilder();
        boolean inside = true;
        for (final var c : s.toCharArray()) {
            if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                cased.append(inside ? Character.toLowerCase(c) : Character.toUpperCase(c));
                inside = true;
            } else {
                inside = false;
            }
        }
        return cased.toString();
    }

    /**
     * Camel case the input starting with a lower case letter. Note that the macro that lower cases all characters is
     * called {@link Case.Lower}
     */
    public static class LowCamel implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            return camelCase(in.toString().trim());
        }
    }

    /**
     * Camel case the input starting with an upper case letter. Note that the macro that upper cases all characters is
     * called {@link Case.Upper}
     */
    public static class UppCamel implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            return Case.capitalize(camelCase(in.toString().trim()));
        }
    }

    /**
     * Converts the input into Cstyle variable name.
     * Converts a camel cased word into a C style all upper case variable name. For example
     *{@code _thisIsACamelCaseWord -> THIS_IS_A_CAMEL_CASE_WORD}
     * The separator is the first non-space character after the name of the macro. In the example it is underscore
     * but it can be
     *
     * {@code :thisIsACamelCaseWord -> THIS:IS:A:CAMEL:CASE:WORD}
     *
     */
    public static class CStyle implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            InputHandler.skipWhiteSpaces(in);
            char sep = in.charAt(0);
            InputHandler.skip(in, 1);
            return cstyle(in.toString(), sep);
        }
    }

    /**
     * Converts a camel cased word into a sentence. For exmple
     * {@code thisIsACamelCaseWord -> "this is a camel case word"}
     */
    public static class Sentence implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            InputHandler.skipWhiteSpaces(in);
            return sentence(in.toString().trim());
        }
    }

}
