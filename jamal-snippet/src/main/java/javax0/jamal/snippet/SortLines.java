package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.util.Arrays;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class SortLines implements Macro {

	@Override
	public String evaluate(Input in, Processor processor) throws BadSyntax {
		skipWhiteSpaces(in);
		boolean endsWithNewline = in.toString().endsWith("\n");
		String[] lines = in.toString().split("\n");
		Arrays.sort(lines);
		return String.join("\n", lines) + (endsWithNewline ? "\n" : "");
	}

}
