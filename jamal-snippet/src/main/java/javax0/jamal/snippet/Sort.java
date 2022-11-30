package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Range;
import javax0.jamal.tools.Scan;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.Params.holder;

public class Sort implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        // snippet sort_options
        final var separator = holder("separator").orElse("\n").asPattern();
        // specifies the separator regular expression, that separates the individual records.
        // The default value if `\n`, which means the lines are the records.
        final var join = holder(null, "join").orElse("\n").asString();
        // is the string to use to join the records together after the sorting was done.
        // The default value is the `\n` string (not pattern), that means the records will be individual lines in the output.
        final var locale = holder(null, "locale", "collatingOrder", "collator").asString();
        // can define the locale for the sorting.
        // The default locale `en-US.UTF-8`.
        // Any locale string can be used installed in the Java environment and passed to the method `Locale.forLanguageTag()`.
        // When this option used with the alias `collator` the value of the option has to be the fully qualified name of a class extending the `java.text.Collator` abstract class.
        // The class will be instantiated and used to sort the records.
        // Using this option this way makes it possible to use special purpose collator, like the readily available `javax0.jamal.snippet.SemVerCollator`.
        // This collator will sort the records treating the keys as software version numbers that follow the semantic versioning standard.
        final var columns = holder(null, "columns").asString();
        // can specify the part of the textual record to be used as sorting key.
        // The format of the parameter is `n..m` where `n` is the first character position and `m-1` is the last character position to be used.
        // The values can run from 1 to the maximum number of characters.
        // If you specify column values that run out of the line length then the macro will result an error.
        final var pattern = holder(null, "pattern").asPattern();
        // can specify a regular expression pattern to define the part of the line as sort key.
        // The expression may contain matching groups.
        // In that case the strings matching the parts between the parentheses are appended from left to right and used as a key.
        // This option must not be used together with the option `columns`.
        final var numeric = holder(null, "numeric").asBoolean();
        // will sort based on the numeric order of the keys.
        // In this case the keys must be numeric or else the conversion to `BigDecimal` before the sort will fail.
        final var reverse = holder(null, "reverse").asBoolean();
        // do the sorting in reverse order.
        // end snippet
        Scan.using(processor)
                .from(this)
                .firstLine()
                .keys(separator, join, locale, columns, pattern, numeric, reverse)
                .parse(in);
        Collator collator = getCollator(locale);

        BadSyntax.when(pattern.isPresent() && columns.isPresent(),  "Can not use both options '%s' and '%s' together.", pattern.name(), columns.name());

        skipWhiteSpaces(in);
        Stream<LineHolder<String>> lines = Arrays.stream(in.toString().split(separator.get().pattern(), -1))
                .map(s -> new LineHolder<>(s, s));
        if (columns.isPresent()) {
            List<Range> ranges = Range.calculateFrom(columns.get(), Integer.MAX_VALUE);
            BadSyntax.when(ranges.size() != 1,  "The option '%s' can only have a single range value!", columns.name());
            Range range = ranges.get(0);
            lines = lines.map(line -> new LineHolder<>(line.original, line.original.substring(range.from - 1, range.to - 1)));
        } else if (pattern.isPresent()) {
            lines = lines.map(findMatches(pattern));
        }

        final List<String> values;
        try {
            values = (numeric.is() ?
                    lines.map(line -> new LineHolder<>(line.original, new BigDecimal(line.key)))
                            .sorted(Comparator.comparing(LineHolder::key))
                    :
                    lines.sorted(Comparator.comparing(LineHolder::key, collator)))
                    .map(LineHolder::original).collect(toList());
        } catch (final StringIndexOutOfBoundsException e) {
            throw new BadSyntax("Column specification does not fit the lines", e);
        } catch (final NumberFormatException e) {
            throw new BadSyntax("Numeric sorting on non numeric values", e);

        }
        if (reverse.is()) {
            Collections.reverse(values);
        }
        return String.join(join.get(), values);
    }

    private Collator getCollator(final Params.Param<String> locale) throws BadSyntax {
        if (locale.isPresent() && locale.name().equals("collator")) {
            try {
                if ("semver".equalsIgnoreCase(locale.get())) {
                    return new SemVerCollator();
                }
                final var collator = Class.forName(locale.get()).getConstructor().newInstance();
                if (collator instanceof Collator) {
                    return (Collator) collator;
                } else {
                    throw new BadSyntax(String.format("collator class '%s' is not a collator", locale.get()));
                }
            } catch (Exception e) {
                throw new BadSyntax(String.format("collator class '%s' cannot be instantiated", locale.get()), e);
            }
        }
        return Collator.getInstance(getLocaleFromParam(locale));
    }

    private Locale getLocaleFromParam(Params.Param<String> locale) throws BadSyntax {
        if (locale.isPresent()) {
            return Locale.forLanguageTag(locale.get());
        } else {
            return Locale.forLanguageTag("en-US.UTF-8");
        }
    }

    /**
     * Returns a Function that converts a line holder to a new one, which uses the part of the line as a key that
     * matches the pattern.
     *
     * @param pattern is a parameter, and it is guaranteed to be present when this method is invoked.
     *                When a line does not match the pattern, the whole line is used as key.
     * @return the function to map the line holders to new line holders matching the patterns as key
     * @throws BadSyntax if the pattern cannot be acquired
     */
    private Function<LineHolder<String>, LineHolder<String>> findMatches(Params.Param<Pattern> pattern) throws BadSyntax {
        Pattern p = pattern.get();
        return line -> {
            var matcher = p.matcher(line.original);
            if (matcher.find()) {
                final var key = new StringBuilder();
                if (matcher.groupCount() > 0) {
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        key.append(matcher.group(i));
                    }
                } else {
                    key.append(matcher.group());
                }
                return new LineHolder<>(line.original, key.toString());
            } else {
                return new LineHolder<>(line.original, line.original);
            }
        };
    }

    /**
     * A line holder holds one record. It is called line holder because records are usually lines when the default
     * record separator, {@code \n} is used.
     * <p>
     * The holder stores the original record and a KEY usually calculated from the record.
     * Both the original string and the key can be queried from the object.
     *
     * @param <KEY> the type of the key.
     */
    private static class LineHolder<KEY extends Comparable<KEY>> {
        private final String original;
        private final KEY key;

        LineHolder(String original, KEY key) {
            this.original = original;
            this.key = key;
        }

        public String original() {
            return original;
        }

        public KEY key() {
            return key;
        }

    }

}
