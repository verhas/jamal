package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Snip implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        skipWhiteSpaces(in);
        final String id;
        id = InputHandler.fetchId(in);
        skipWhiteSpaces(in);
        if (firstCharIs(in, '/')) {
            skip(in, 1);
            final var regexPart = in.toString();
            final var lastIndex = regexPart.lastIndexOf('/');
            if (lastIndex == -1) {
                throw new BadSyntax("The regular expression at after the snippet ID should be enclosed between '/' characters");
            }
            final var lines = SnippetStore.getInstance(processor).snippet(id).split("\n");
            final var regex = regexPart.substring(0, lastIndex);
            try {
                if (lines.length == 0 || lines[0].length() == 0) {
                    return ""; // snippet is empty
                }
                final var pattern = Pattern.compile(regex);
                final var matcher = pattern.matcher(lines[0]);
                if (matcher.find()) {
                    if (matcher.groupCount() > 0) {
                        return matcher.group(1);
                    } else {
                        throw new BadSyntax("The regular expression /" + regex + "/ does not contain capturing group.");
                    }
                } else {
                    throw new BadSyntax("The regular expression /" + regex + "/ cannot be found in the line '" + lines[0] + "'");
                }
            } catch (PatternSyntaxException e) {
                throw new BadSyntax("The evaluating the regular expression /" + regex + "/ on the line '" + lines[0] + "' resulted an exception.", e);
            }
        }
        // the rest of the input is ignored
        return SnippetStore.getInstance(processor).snippet(id);
    }
}
