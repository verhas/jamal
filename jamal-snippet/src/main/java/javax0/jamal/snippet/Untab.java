package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.util.Collections;

/**
 * Take the argument of the macro and removes N spaces from the start of each line so that there is at least one line
 * that does not start with a space character.
 * <p>
 * This can be used, when a snippet is included into the macro file and some program code is tabulated. In that case
 * this snippet will be moved to the left as much as possible.
 */
public class Untab implements Macro, InnerScopeDependent, BlockConverter {
    @Override
    public String getId() {
        return "untab";
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var tabSize = Params.<Integer>holder("tabSize", "tab", "size").orElseInt(8);
        Params.using(processor).from(this).keys(tabSize).parse(in);
        final var sb = in.getSB();
        convertTextBlock(sb, in.getPosition(), tabSize);
        return sb.toString();
    }

    @Override
    public void convertTextBlock(final StringBuilder sb, final Position pos, final Params.Param<?>... params) throws BadSyntax {
        assertParams(1, params);
        final var tabSize = params[0].asInt();

        if (tabSize.get() > 0) {
            convertTabs(sb, tabSize.get());
        } else {
            throw new BadSyntax("The tab size must be greater than zero");
        }
    }

    private void convertTabs(final StringBuilder sb, final Integer n) {
        final var tabSpaces = String.join("", Collections.nCopies(n, " "));
        while (true) {
            final var lastTabPosition = sb.lastIndexOf("\t");
            if (lastTabPosition == -1) {
                break;
            }
            final var lineStart = sb.lastIndexOf("\n", lastTabPosition);
            int tabPosition;
            while ((tabPosition = sb.indexOf("\t", lineStart)) != -1) {
                final var spaces = n - (tabPosition - lineStart) % n + 1;
                sb.replace(tabPosition, tabPosition + 1, tabSpaces.substring(0, spaces));
            }
        }
    }
}