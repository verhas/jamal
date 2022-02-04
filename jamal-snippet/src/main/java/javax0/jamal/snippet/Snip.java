package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Snip implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var poly = Params.holder(null, "poly").asBoolean();
        Params.using(processor).from(this).between("()").keys(poly).parse(in);
        skipWhiteSpaces(in);
        final String id;
        final String text;
        if (poly.is()) {
            id = in.toString();
            text = SnippetStore.getInstance(processor).snippet(Pattern.compile(id));
        } else {
            id = InputHandler.fetchId(in);
            skipWhiteSpaces(in);
            text = SnippetStore.getInstance(processor).snippet(id);
        }
        if (!poly.is() && firstCharIs(in, '/')) {
            skip(in, 1);
            final var regexPart = in.toString();
            final var lastIndex = regexPart.lastIndexOf('/');
            if (lastIndex == -1) {
                throw new BadSyntax("The regular expression following the snippet ID should be enclosed between '/' characters");
            }
            final var lines = text.split("\n");
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
                throw new BadSyntax("The evaluating the regular expression /" + regex + "/ on the line '" + lines[0] + "' resulted an exception: "+e.getMessage(), e);
            }
        } else {
            // the rest of the input is ignored
            return text;
        }
    }
}
