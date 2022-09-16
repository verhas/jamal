package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;

import java.util.ArrayList;
import java.util.List;

/**
 * A range is a pair of negative, or positive integers, representing the start and the end of a range.
 * It is primarily used to denote line ranges.
 */
public class Range {

    public static class Lines {
        public static void filter(final StringBuilder sb, final String rangesSpecification) throws BadSyntax {
            final var lines = sb.toString().split("\n", -1);
            for (int i = 0; i < lines.length - 1; i++) {
                lines[i] = lines[i] + "\n";
            }
            final var ranges = Range.calculateFrom(rangesSpecification, lines.length);
            sb.setLength(0);
            boolean nlMissing = false;
            for (final var range : ranges) {
                final int step = range.to >= range.from ? 1 : -1;
                if (nlMissing) {
                    sb.append("\n");
                }
                for (int i = range.from; i != range.to + step; i += step) {
                    if (i <= lines.length && i > 0) {
                        final var line = lines[i - 1];
                        sb.append(line);
                        nlMissing = line.length() == 0 || line.charAt(line.length() - 1) != '\n';
                    }
                }
            }
        }
    }

    final public int from;
    final public int to;

    private Range(final int from, final int to) {
        this.from = from;
        this.to = to;
    }

    public static Range range(final int from, final int to) {
        return new Range(from, to);
    }

    /**
     * Calculate the list of the ranges given as a textual list in the string {@code s}.
     * <p>
     * The string can contain one or more ange definitions.
     * Range definitions are separated by commas {@code ,} or by semicolon {@code ;} or these mixed.
     * Ranges can overlap, repeated, and can have direction starting with a value greater than the end value.
     * <p>
     * A range definition can be a pure number {@code N}, which is the same as {@code N..N}, or a range definition in
     * the format {@code START .. END}. Here {@code START} is the first value in the range and {@code END} is the last.
     * <p>
     * The {@code START} and {@code END} can be positive or negative, but it cannot be zero.
     * <p>
     * Negative values are corrected counting from the end of the range.
     * If a range {@code START} and/or {@code END} value is larger than {@code n} or smaller than {@code -n} then the
     * range will contain the value, which is out of the allowed values.
     * <p>
     * It is up to the caller to interpret negative (after correction) and larger than {@code n} values.
     * It is also the caller responsibility to use the end values exclusive or inclusive.
     * Usually the start value is inclusive and the end value is exclusive in Java, like in {@code substring()}.
     * <p>
     * On the other hand, the caller may not use ths values as zero based, because zero is not an allowed value.
     * This is because these ranges are used to specify lines and in the different editors the line numbering starts
     * with one.
     *
     * @param s the string with the list of the ranges
     * @param n the maximum number that can be in the range. When a number in a range is negative then it is calculated
     *          from this number backwards (like in Python, when you specify an array split as {@code a[5:-2]}).
     * @return a list of ranges with line numbers in it.
     * @throws BadSyntax if the formatting of the string is incorrect or a value is zero.
     */
    public static List<Range> calculateFrom(final String s, final int n) throws BadSyntax {
        final var rangeSpecs = s.split("[,;]");
        final var ranges = new ArrayList<Range>();
        for (final var range : rangeSpecs) {
            var startStop = range.split("\\.\\.");
            if (startStop.length == 1) {
                startStop = new String[]{range, range};
            }
            if (startStop.length != 2) {
                throw new BadSyntax("The line range " + range + " is not valid");
            }
            final int to, from;
            try {
                from = Integer.parseInt(startStop[0].trim());
                to = Integer.parseInt(startStop[1].trim());
            } catch (NumberFormatException nfe) {
                throw new BadSyntax("The line range " + range + " is not valid");
            }
            if (from == 0 || to == 0 || from == -n || to == -n) {
                throw new BadSyntax("The line range " + range + " is not valid");
            }
            ranges.add(range(correct(from, n), correct(to, n)));
        }
        return ranges;
    }

    private static int correct(final int from, final int n) {
        if (from < 0) {
            return from + n;
        } else {
            return from;
        }
    }

}
