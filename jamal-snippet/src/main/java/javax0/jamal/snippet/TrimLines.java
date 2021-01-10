package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.MacroReader;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces2EOL;

/**
 * Take the argument of the macro and removes N spaces from the start of each line so that there is at least one line
 * that does not start with a space character.
 * <p>
 * This can be used, when a snippet is included into the macro file and some program code is tabulated. In that case
 * this snippet will be moves to the left as much as possible.
 */
public class TrimLines implements Macro, InnerScopeDependent {
    @Override
    public String getId() {
        return "trimLines";
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var reader = MacroReader.macro(processor).integer();
        final var margin = reader.readValue("margin").orElse(0);
        skipWhiteSpaces2EOL(in);
        final var sb = in.getSB();
        int minSpaces = Integer.MAX_VALUE;
        for (int i = 0; i < sb.length(); ) {
            int spaceCount = 0;
            while (i < sb.length() && Character.isWhitespace(sb.charAt(i)) && sb.charAt(1) != '\n') {
                i++;
                spaceCount++;
            }
            minSpaces = Math.min(minSpaces, spaceCount);
            int index = sb.indexOf("\n", i);
            if (index == -1) break;
            i = index + 1;
        }
        minSpaces -= margin;
        for (int i = 0; i < sb.length(); ) {
            if (minSpaces < 0) {
                sb.insert(i," ".repeat(-minSpaces));
            } else {
                sb.delete(i, i + minSpaces);
            }
            int index = sb.indexOf("\n", i);
            if (index == -1) break;
            i = index + 1;
        }
        return sb.toString();
    }
}
