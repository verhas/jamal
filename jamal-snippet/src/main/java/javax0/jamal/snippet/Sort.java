package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.Params.holder;

public class Sort implements Macro, InnerScopeDependent {

	@Override
	public String evaluate(Input in, Processor processor) throws BadSyntax {
		final var separator = holder("separator").orElse("\n").asPattern();
		final var join = holder("join").orElse("\n").asString();
		final var locale = holder("locale").asString();
		final var columns = holder("columns").asString();
		final var pattern = holder("pattern").asPattern();
		final var numeric = holder("numeric").asBoolean();
		final var reverse = holder("reverse").asBoolean();
		Scan.using(processor)
				.from(this)
				.firstLine()
				.keys(separator, join, locale, columns, pattern, numeric, reverse)
				.parse(in);

		if (pattern.isPresent() && columns.isPresent()) {
			throw new BadSyntax("Can not use both options 'pattern' and 'columns' together.");
		}

		skipWhiteSpaces(in);
		Stream<LineHolder> lines = new ArrayList<>(Arrays.asList(in.toString().split(separator.get().pattern(), -1)))
				.stream()
				.map(s -> new LineHolder(s, s));
		if (columns.isPresent()) {
			String[] columnParts = splitColumns(columns);
			int begin = safeParse(columnParts[0]);
			int end = safeParse(columnParts[1]);
			lines = lines.map(line -> new LineHolder(line.original, line.original.substring(begin-1, end-1)));
		}
		if (pattern.isPresent()) {
			Pattern p = pattern.get();
			lines = lines.map(findMatches(p));
		}
		if (numeric.is()) {
			lines = lines
					.map(line -> new IntLineHolder(line.original, Integer.parseInt(line.extracted)))
					.sorted(Comparator.comparingInt(IntLineHolder::value))
					.map(intLine -> new LineHolder(intLine.original, String.valueOf(intLine.value)));
		} else {
			if (locale.isPresent()) {
				Locale language = Locale.forLanguageTag(locale.get());
				lines = lines.sorted(Comparator.comparing(LineHolder::extracted, Collator.getInstance(language)));
			} else {
				lines = lines.sorted(Comparator.comparing(LineHolder::extracted));
			}
		}
		List<String> values = lines.map(LineHolder::original).collect(toList());
		if (reverse.is()) {
			Collections.reverse(values);
		}
		return String.join(join.get(), values);
	}

	private Function<LineHolder, LineHolder> findMatches(Pattern p) {
		return line -> {
			var matcher = p.matcher(line.original);
			matcher.find();
			return new LineHolder(line.original, matcher.group());
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
			throw new BadSyntax("Expected exactly 2 parameters for option 'columns', got " + parts.length);
		}
		return parts;
	}

	private static class LineHolder {
		private final String original;
		private final String extracted;

		LineHolder(String original, String extracted) {
			this.original = original;
			this.extracted = extracted;
		}

		LineHolder(String value) {
			this.original = value;
			this.extracted = value;
		}

		public String original() {
			return original;
		}

		public String extracted() {
			return extracted;
		}

	}

	private static class IntLineHolder {
		private final String original;
		private final int value;

		IntLineHolder(String original, int value) {
			this.original = original;
			this.value = value;
		}

		int value() {
			return value;
		}
	}
}
