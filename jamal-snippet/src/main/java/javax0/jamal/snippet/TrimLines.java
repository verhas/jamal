package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

/**
 * Take the argument of the macro and removes N spaces from the start of each line so that there is at least one line
 * that does not start with a space character.
 * <p>
 * This can be used, when a snippet is included into the macro file and some program code is tabulated. In that case
 * this snippet will be moved to the left as much as possible.
 */
public class TrimLines implements Macro, InnerScopeDependent, BlockConverter {
    @Override
    public String getId() {
        return "trimLines";
    }

    // end snippet
    // snippet trimLineStart
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var margin = Params.<Integer>holder("margin").orElseInt(0);
        final var trimVertical = Params.<Boolean>holder("trimVertical").asBoolean();
        final var verticalTrimOnly = Params.<Boolean>holder("verticalTrimOnly", "vtrimOnly").asBoolean();
        Params.using(processor).from(this).keys(margin, trimVertical, verticalTrimOnly).parse(in);
        //end snippet
        final var sb = in.getSB();
        convertTextBlock(sb, in.getPosition(), margin, trimVertical, verticalTrimOnly);
        return sb.toString();
    }

    @Override
    public void convertTextBlock(final StringBuilder sb, final Position pos, final Params.Param<?>... params) throws BadSyntax {
        checkNumberOfParams(3, params);
        final var margin = params[0].asInt();
        final var trimVertical = params[1].asBoolean();
        final var verticalTrimOnly = params[2].asBoolean();

        if (!verticalTrimOnly.is()) {
            trimHorizontal(sb, margin.get());
        }
        if (trimVertical.is() || verticalTrimOnly.is()) {
            trimVertical(sb);
        }
    }


    private static void trimVertical(StringBuilder sb) {
        int i = 0;
        while ( i < sb.length() && sb.charAt(i) == '\n') {
            i++;
        }
        sb.delete(0, i);
        while (sb.length() > 1 && sb.charAt(sb.length() - 1) == '\n' && sb.charAt(sb.length() - 2) == '\n') {
            sb.delete(sb.length() - 1, sb.length());
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.delete(sb.length() - 1, sb.length());
        }
    }

    private static void trimHorizontal(StringBuilder sb, int requestedMargin) throws BadSyntax {
        final int margin = calculateMargin(sb, requestedMargin);
        doTrimming(sb, margin);
    }

    private static void doTrimming(StringBuilder sb, int margin) {
        for (int i = 0; i < sb.length(); ) {
            if (margin < 0) {
                sb.insert(i, " ".repeat(-margin));
            } else {
                int nlIndex = sb.indexOf("\n", i);
                if ((nlIndex >= i + margin) || nlIndex == -1) {
                    sb.delete(i, i + margin);
                }
            }
            int index = sb.indexOf("\n", i);
            if (index == -1) break;
            int j = index - 1;
            while (i <= j && Character.isWhitespace(sb.charAt(j))) {
                j--;
            }
            sb.delete(j + 1, index);
            i = j + 2;
        }
    }

    private static int calculateMargin(StringBuilder sb, int margin) {
        int minSpaces = Integer.MAX_VALUE;
        for (int i = 0; i < sb.length(); ) {
            int spaceCount = 0;
            while (i < sb.length() && Character.isWhitespace(sb.charAt(i)) && sb.charAt(i) != '\n') {
                i++;
                spaceCount++;
            }
            if (i < sb.length() && sb.charAt(i) != '\n') {
                minSpaces = Math.min(minSpaces, spaceCount);
            }
            int index = sb.indexOf("\n", i);
            if (index == -1) break;
            i = index + 1;
        }
        if (minSpaces == Integer.MAX_VALUE) {
            minSpaces = 0;
        }
        minSpaces -= margin;
        return minSpaces;
    }
}