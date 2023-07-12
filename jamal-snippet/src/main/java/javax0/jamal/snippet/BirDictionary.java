package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

import java.util.HashMap;
import java.util.Map;

public class BirDictionary implements Macro, Scanner.FirstLine {
    static final String DEFAULT_DICTIONARY_NAME = "bir$dictionary";

    static class BirDictonary implements Identified {
        private final String name;
        final Map<String, Integer> dictionary;

        int get(String key) {
            if (dictionary.containsKey(key)) {
                return dictionary.get(key);
            }
            return -1;
        }

        BirDictonary(String name, Map<String, Integer> dictionary) {
            this.name = name;
            this.dictionary = dictionary;
        }

        @Override
        public String getId() {
            return name;
        }
    }

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
