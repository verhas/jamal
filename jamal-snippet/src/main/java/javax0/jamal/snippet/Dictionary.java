package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;

import java.util.HashMap;

public class Dictionary implements Macro, Scanner.FirstLine {
    // snipline DEFAULT_DICTIONARY_NAME filter="(.*)"
    static final String DEFAULT_DICTIONARY_NAME = "decor$dictionary";

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
            int pos = getPos(line);
            if (pos < 0) {
                pos = line.length();
            } else {
                line = line.substring(0, pos) + line.substring(pos + 1);
            }
            dictionary.put(line, pos);
        }
        processor.getRegister().define(new Decorator.Dictionary(macroName.get(), dictionary));
        return "";
    }

    private static int getPos(String line) {
        for( int i = 0 ; i < line.length(); i++ ){
            if( !Character.isLetter(line.charAt(i)) ){
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getId() {
        return "dictionary";
    }
}
