package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

import java.util.function.Function;

public class Replace implements Macro, InnerScopeDependent, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var isRegex = scanner.bool("regex");
        final var detectNoChange = scanner.bool("detectNoChange");
        scanner.done();
        InputHandler.skipWhiteSpaces(in);
        final var parts = ReplaceUtil.chop(InputHandler.getParts(in, processor));

        BadSyntax.when(parts.length < 2, () -> String.format("Marco 'replace' needs at least two arguments, got only %d:\n%s\n----------",
                parts.length, String.join("\n", parts)));
        String string = parts[0];
        for (int i = 1; i < parts.length; i += 2) {
            final var from = parts[i];
            if( from.isEmpty() ){
                throw new BadSyntax("You cannot replace an empty string.");
            }
            final String to = ReplaceUtil.fetchElement(parts, i + 1);
            if (isRegex.is()) {
                try {
                    string = getOrThrow(string, s -> s.replaceAll(from, to), detectNoChange.is(), from);
                } catch (IllegalArgumentException e) {
                    throw new BadSyntax("There is a problem with the regular expression in macro 'replace' : "
                            + from + "\n" + to + "\n", e);
                }
            } else {
                string = getOrThrow(string, s -> s.replace(from, to), detectNoChange.is(), from);
            }
        }
        return string;
    }

    /**
     * Return the modified string or throw BadSyntax if the string was not modified, and the detectNoChange parameter
     * is set.
     *
     * @param original       the original string that was used to create the modified one
     * @param converter      function that converts the string
     * @param detectNoChange flag to throw BadSyntax if the string was not modified
     * @param from           the string that was replaced. Used only to create the error message.
     * @return the modified (or non-modified) string or throws BadSyntax
     * @throws BadSyntax when the original and the modified strings are not the same and detectNoChange is true
     */
    private static String getOrThrow(final String original,
                                     final Function<String, String> converter,
                                     final boolean detectNoChange,
                                     final String from) throws BadSyntax {
        try {
            final var modified = converter.apply(original);
            if (detectNoChange && modified.equals(original)) {
                throw new BadSyntax("Macro 'replace' cannot find the string to replace: " + from);
            }
            return modified;
        } catch (IllegalArgumentException e) {
            throw new BadSyntax("There is a problem with the regular expression '"
                    + from + "' in macro 'replace'", e);
        }
    }
}
/*template jm_replace
{template |replace|replace ($O$ $DNC$) $S$$string$$S$$search_1$$S$$replace_1$$S$$search_x$$S$$replace_x$$S$|replace the text|
  {variable |O|enum("regex","")}
  {variable |DNC|enum("detectNoChange","")}
  {variable |S|enum("/","~","`...`")}
  {variable |string|""}
  {variable |search_1|"search_1"}
  {variable |replace_1|"replace_1"}
  {variable |search_x|"search_x"}
  {variable |replace_x|"replace_x"}
}
 */