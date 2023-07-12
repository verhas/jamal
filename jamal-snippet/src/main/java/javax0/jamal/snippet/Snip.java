package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.SHA256;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.StringParameter;

import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static javax0.jamal.tools.InputHandler.*;

public class Snip implements Macro, Scanner {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var pos = in.getPosition();
        final var scanner = newScanner(in, processor);
        final var poly = scanner.bool(null, "poly");
        final var hashString = scanner.str("hash", "hashCode").defaultValue(null);
        final var extraParams = scanner.extra();
        scanner.done();

        final var transformer = processor.getRegister().getMacro("snip:transform")
                .filter(m -> m instanceof SnipTransform).map(m -> (SnipTransform) m)
                .orElseThrow(() -> new BadSyntax("The macro 'snip:transform' is not registered"));

        skipWhiteSpaces(in);
        final String id;
        final String text;
        if (poly.is()) {
            id = in.toString();
            text = SnippetStore.getInstance(processor).snippet(Pattern.compile(id));
            checkHashString(hashString, id, text, pos);
            return transformer.evaluate(extraParams, javax0.jamal.tools.Input.makeInput(text, pos), processor);
        } else {
            id = InputHandler.fetchId(in);
            skipWhiteSpaces(in);
            text = SnippetStore.getInstance(processor).snippet(id);
            checkHashString(hashString, id, text, pos);

            if (firstCharIs(in, '/')) {
                return getRegexMatchedFromTheFirstLine(in, text);
            } else {
                return transformer.evaluate(extraParams, javax0.jamal.tools.Input.makeInput(text, pos), processor);
            }
        }
    }

    private String getRegexMatchedFromTheFirstLine(final Input in, final String text) throws BadSyntax {
        skip(in, 1);
        final var regexPart = in.toString();
        final var lastIndex = regexPart.lastIndexOf('/');
        BadSyntax.when(lastIndex == -1, "The regular expression following the snippet ID should be enclosed between '/' characters");
        final var lines = text.split("\n");
        final var regex = regexPart.substring(0, lastIndex);
        try {
            if (lines.length == 0 || lines[0].length() == 0) {
                return "";
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
            throw new BadSyntax("The evaluating the regular expression /" + regex + "/ on the line '" + lines[0] + "' resulted an exception: " + e.getMessage(), e);
        }
    }

    private static void checkHashString(StringParameter hashString,
                                        String id,
                                        String text,
                                        Position pos) throws BadSyntax {
        if (!hashString.isPresent()) {
            return;
        }
        final var hashStringCalculated = HexDumper.encode(SHA256.digest(text));
        final var hash = hashString.get().replaceAll("\\.", "").toLowerCase(Locale.ENGLISH);
        if (hash.length() < SnipCheck.MIN_LENGTH) {
            BadSyntax.when(hashStringCalculated.contains(hash), () -> String.format("The %s hash is '%s'. '%s' is too short, you need at least %d characters.\n",
                    id, SnipCheck.doted(hashStringCalculated), hashString.get(), SnipCheck.MIN_LENGTH));
            throw new BadSyntax(String.format("The %s hash is '%s', not '%s', which is too short anyway, you need at least %d characters.\n", id, SnipCheck.doted(hashStringCalculated), hashString.get(), SnipCheck.MIN_LENGTH));
        }
        if (!hashStringCalculated.contains(hash)) {
            throw new SnipCheckFailed(id, SnipCheck.doted(hashStringCalculated), hashString.get(), null, pos);
        }
    }
}
