package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Marker;
import javax0.jamal.tools.Params;

import java.util.ArrayList;
import java.util.List;

import static javax0.jamal.tools.FileTools.absolute;
import static javax0.jamal.tools.FileTools.getInput;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

@Macro.Stateful
public class Include implements Macro {
    /**
     * Count the depth of the includes. In case this is more than 100 stop the processing. Most likely this is a wrong
     * recursive include that would cause stack overflow.
     */
    private int depth = getDepth();

    private static final String DEFAULT_DEPTH = "100";

    private static int getDepth() {
        final String limitString = EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_INCLUDE_DEPTH_ENV).orElse(DEFAULT_DEPTH);
        try {
            return Integer.parseInt(limitString);
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(new BadSyntax("The environment variable " + EnvironmentVariables.JAMAL_INCLUDE_DEPTH_ENV + " should be an integer"));
        }
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        var position = input.getPosition();
        final var top = Params.<Boolean>holder(null, "top").asBoolean();
        final var verbatim = Params.<Boolean>holder("includeVerbatim", "verbatim").asBoolean();
        final var lines = Params.<Boolean>holder(null, "lines").asString();
        final var noCache = Params.<Boolean>holder(null, "noCache").asBoolean();
        Params.using(processor).from(this).between("[]").keys(verbatim, top, lines, noCache).parse(input);

        position = repositionToTop(position, top);

        skipWhiteSpaces(input);
        var reference = position.file;
        var fileName = absolute(reference, input.toString().trim());
        if (depth-- == 0) {
            depth = getDepth(); // try macro may recover
            throw new BadSyntax("Include depth is too deep");
        }
        var marker = new Marker("{@include " + fileName + "}", position);
        final String result;
        processor.getRegister().push(marker);
        final var includedInput = getInput(fileName, position, noCache.is());
        if (lines.isPresent()) {
            filterLines(includedInput, lines.get());
        }
        if (verbatim.get()) {
            result = includedInput.toString();
        } else {
            result = processor.process(includedInput);
        }
        processor.getRegister().pop(marker);
        depth++;
        return result;
    }

    private static class Range {
        final int from;
        final int to;

        private Range(final int from, final int to) {
            this.from = from;
            this.to = to;
        }
    }

    private static Range range(final int from, final int to) {
        return new Range(from, to);
    }

    private void filterLines(final Input in, final String s) throws BadSyntax {
        final var sb = in.getSB();
        final var lines = sb.toString().split("\n", -1);
        for (int i = 0; i < lines.length - 1; i++) {
            lines[i] = lines[i] + "\n";
        }
        final var ranges = calculateRanges(s, lines.length);
        sb.setLength(0);
        for (final var range : ranges) {
            final int step = range.to >= range.from ? 1 : -1;
            for (int i = range.from; i != range.to + step; i += step) {
                if (i <= lines.length && i > 0) {
                    sb.append(lines[i - 1]);
                }
            }
        }
    }

    private List<Range> calculateRanges(final String s, final int n) throws BadSyntax {
        final var lines = s.split("[,;]");
        final var ranges = new ArrayList<Range>();
        for (final var line : lines) {
            var range = line.split("\\.\\.");
            if (range.length == 1) {
                range = new String[]{line, line};
            }
            if (range.length != 2) {
                throw new BadSyntax("The line range " + line + " is not valid");
            }
            int to, from;
            try {
                from = Integer.parseInt(range[0].trim());
                to = Integer.parseInt(range[1].trim());
            } catch (NumberFormatException nfe) {
                throw new BadSyntax("The line range " + line + " is not valid");
            }
            if (from < 0) {
                from += n;
            }
            if (to < 0) {
                to += n;
            }
            if (from == 0 || to == 0) {
                throw new BadSyntax("The line range " + line + " is not valid");
            }
            ranges.add(range(from, to));
        }
        return ranges;
    }

    /**
     * Get the position of the 'include' from the input. When the 'top' parameter is true the position is the position
     * of the root document even if the include macro is used several level deeper in included documents.
     *
     * @param position the position of the include macro from which we calculate the top file position
     * @param top   flag to signal if we need to include from the top
     * @return the effective position used to calculate the file location of the included file when specified as a
     * relative file name
     * @throws BadSyntax if 'top' is erroneous and querying it throws exception
     */
    private Position repositionToTop(Position position, final Params.Param<Boolean> top) throws BadSyntax {
        if (top.is()) {
            while (position.parent != null) {
                position = position.parent;
            }
        }
        return position;
    }
}
