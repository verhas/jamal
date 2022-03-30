package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.SHA256;
import javax0.jamal.tools.Scan;

import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Snip implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var pos = in.getPosition();
        final var poly = Params.holder(null, "poly").asBoolean();
        final var hashString = Params.<String>holder("hash", "hashCode").orElse(null);
        Scan.using(processor).from(this).between("()").keys(poly, hashString).parse(in);
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
        if (hashString.isPresent()) {
            checkHashString(hashString, id, text, pos);
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
                throw new BadSyntax("The evaluating the regular expression /" + regex + "/ on the line '" + lines[0] + "' resulted an exception: " + e.getMessage(), e);
            }
        } else {
            // the rest of the input is ignored
            return text;
        }
    }

    private static void checkHashString(Params.Param<String> hashString,
                                        String id,
                                        String text,
                                        Position pos) throws BadSyntax {
        final var hashStringCalculated = HexDumper.encode(SHA256.digest(text));
        final var hash = hashString.get().replaceAll("\\.", "").toLowerCase(Locale.ENGLISH);
        if (hash.length() < SnipCheck.MIN_LENGTH) {
            if (hashStringCalculated.contains(hash)) {
                throw new BadSyntax("The " + id + " hash is '" + SnipCheck.doted(hashStringCalculated) + "'. '" +
                        hashString.get() + "' is too short, you need at least " + SnipCheck.MIN_LENGTH +
                        " characters.\n");
            } else {
                throw new BadSyntax("The " + id + " hash is '" + SnipCheck.doted(hashStringCalculated) + "', not '" +
                        hashString.get() + "', which is too short anyway, you need at least " + SnipCheck.MIN_LENGTH +
                        " characters.\n");
            }
        }
        if (!hashStringCalculated.contains(hash)) {
            throw new SnipCheckFailed(id, SnipCheck.doted(hashStringCalculated), hashString.get(), null, pos);
        }
    }
}
