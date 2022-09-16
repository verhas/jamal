package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Range;
import javax0.jamal.tools.Scan;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.Params.holder;

public class Sort implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var separator = holder("separator").orElse("\n").asPattern();
        final var join = holder(null, "join").orElse("\n").asString();
        final var locale = holder(null, "locale", "collatingOrder").asString();
        final var columns = holder(null, "columns").asString();
        final var pattern = holder(null, "pattern").asPattern();
        final var numeric = holder(null, "numeric").asBoolean();
        final var reverse = holder(null, "reverse").asBoolean();
        Scan.using(processor)
                .from(this)
                .firstLine()
                .keys(separator, join, locale, columns, pattern, numeric, reverse)
                .parse(in);
        Collator collator = Collator.getInstance(getLocaleFromParam(locale));

        if (pattern.isPresent() && columns.isPresent()) {
            throw new BadSyntax(format("Can not use both options '%s' and '%s' together.", pattern.name(), columns.name()));
        }

        skipWhiteSpaces(in);
        Stream<LineHolder<String>> lines = new ArrayList<>(Arrays.asList(in.toString().split(separator.get().pattern(), -1)))
                .stream()
                .map(s -> new LineHolder<>(s, s));
        if (columns.isPresent()) {
            List<Range> ranges = Range.calculateFrom(columns.get(), -1);
            if (ranges.size() != 1) {
                throw new BadSyntax(format("The option '%s' can only have a single range value!", columns.name()));
            }
            Range range = ranges.get(0);
            lines = lines.map(line -> new LineHolder<>(line.original, line.original.substring(range.from, range.to)));
        }
        lines = lines.map(findMatches(pattern));
        if (numeric.is()) {
            lines = lines
                    .map(line -> new LineHolder<>(line.original, new BigDecimal(line.key)))
                    .sorted(Comparator.comparing(LineHolder::key))
                    .map(intLine -> new LineHolder<>(intLine.original, intLine.key.toPlainString()));
        } else {
            lines = lines.sorted(Comparator.comparing(LineHolder::key, collator));
        }
        List<String> values = lines.map(LineHolder::original).collect(toList());
        if (reverse.is()) {
            Collections.reverse(values);
        }
        return String.join(join.get(), values);
    }

    private Locale getLocaleFromParam(Params.Param<String> locale) throws BadSyntax {
        if (locale.isPresent()) {
            return Locale.forLanguageTag(locale.get());
        } else {
            return Locale.forLanguageTag("en-US.UTF-8");
        }
    }

    private Function<LineHolder<String>, LineHolder<String>> findMatches(Params.Param<Pattern> pattern) throws BadSyntax {
        if (!pattern.isPresent()) {
            return Function.identity();
        }
        Pattern p = pattern.get();
        return line -> {
            var matcher = p.matcher(line.original);
            matcher.find();
            return new LineHolder<>(line.original, matcher.group());
        };
    }

    private int safeParse(String number) throws BadSyntax {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException exception) {
            throw new BadSyntax("Could not parse options 'columns' because of an exception.", exception);
        }
    }

    private String[] splitColumns(Params.Param<String> columns) throws BadSyntax {
        String[] parts = InputHandler.getParts(javax0.jamal.tools.Input.makeInput(columns.get()));
        if (parts.length != 2) {
            throw new BadSyntax(format("Expected exactly 2 parameters for option '%s', got %d", columns.name(), parts.length));
        }
        return parts;
    }

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