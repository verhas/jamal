package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

import java.util.HashMap;
import java.util.Map;

public class BirDictionary implements Macro, Scanner.FirstLine {
    static final String DEFAULT_DICTIONARY_NAME = "bir$dictionary";
    static final String DEFAULT_PF_DICTIONARY_NAME = "bir$pfDict";
    static final String DEFAULT_CM_DICTIONARY_NAME = "bir$cmDict";

    /**
     * Represents a dictionary used for birification, associating strings with their corresponding integer values.
     * The dictionary allows retrieval of the integer value associated with a given string.
     */
    static class BirDictonary implements Identified {
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
        BirDictonary(String name, Map<String, Integer> dictionary) {
            this.name = name;
            this.dictionary = dictionary;
        }

        @Override
        public String getId() {
            return name;
        }
    }

    /**
     * Evaluates the input and processes it to define a BirDictionary in the Processor's register.
     * The method reads the input from the provided Input object and processes it line by line.
     * It extracts the macro name from the input using a scanner, with a default value if not provided.
     * It then creates a new HashMap to store the dictionary entries.
     * Each non-empty line in the input is trimmed and processed as follows:
     * <ul>
     *   <li> If the line contains an asterisk '*', the asterisk is removed from the line, and the remaining part is stored as the key.
     *   <li> If the line does not contain an asterisk, the entire line is considered as the key.
     *   <li> The position of the asterisk (or the length of the line if no asterisk is present) is stored as the value
     *     associated with the key.
     * </ul>
     * Finally, the newly created BirDictionary is defined in the Processor's register with the extracted macro name.
     *
     * @param in        The Input object containing the input to be evaluated.
     * @param processor The Processor object used for processing and registering the BirDictionary.
     * @return An empty string.
     * @throws BadSyntax If an error occurs during processing.
     */
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var macroName = scanner.str(null, "id", "name").defaultValue(DEFAULT_DICTIONARY_NAME);
        scanner.done();

        final var lines = in.toString().split("\n", -1);
        final var dictionary = new HashMap<String, Integer>();
        for (var line : lines) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            int pos = line.indexOf('*');
            if (pos < 0) {
                pos = line.length();
            } else {
                line = line.substring(0, pos) + line.substring(pos + 1);
            }
            dictionary.put(line, pos);
        }
        processor.getRegister().define(new BirDictonary(macroName.get(), dictionary));
        return "";
    }

    @Override
    public String getId() {
        return "bir:dictionary";
    }
}
