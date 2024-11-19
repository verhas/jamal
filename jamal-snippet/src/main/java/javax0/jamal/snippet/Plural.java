package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.snippet.tools.Pluralizer;
import javax0.jamal.tools.Scanner;

import java.util.HashMap;
import java.util.Map;

/**
 * Return the plural form of a word.
 */
public class Plural implements Macro, Scanner {

    public static final String DICTIONARY_NAME = "$:plurals";

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {

        final var dict = processor
                .getRegister()
                .getUserDefined(DICTIONARY_NAME)
                .filter(m -> m instanceof Plural.Dictionary)
                .map(m -> (Plural.Dictionary) m)
                .orElseGet(
                        () -> {
                            final var dictionary = new Plural.Dictionary(DICTIONARY_NAME);
                            processor.getRegister().define(dictionary);
                            return dictionary;
                        }
                );
        if (in.toString().contains("=")) {
            final var keyValue = in.toString().split("=");
            BadSyntax.when(keyValue.length != 2, "The key value pair should be separated by '='");
            keyValue[0] = keyValue[0].trim();
            keyValue[1] = keyValue[1].trim();
            BadSyntax.when(keyValue[0].isEmpty(), "The word should not be empty");
            BadSyntax.when(keyValue[1].isEmpty(), "The plural should not be empty");
            dict.dictionary.put(keyValue[0], keyValue[1]);
            return "";
        }
        return dict.plural(in.toString().trim());
    }

    static class Dictionary implements Identified {
        private final String name;
        final Map<String, String> dictionary;

        String plural(String key) {
            if (dictionary.containsKey(key)) {
                return dictionary.get(key);
            }
            return Pluralizer.pluralize(key);
        }


        Dictionary(String name) {
            this.name = name;
            this.dictionary = new HashMap<>();
        }

        @Override
        public String getId() {
            return name;
        }
    }
}
