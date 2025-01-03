package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;

@Macro.Name({"range", "ranges"})
public class RangeMacro implements Macro, InnerScopeDependent, BlockConverter, Scanner.FirstLine {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var ranges = scanner.str(null, "range", "ranges", "lines").optional();
        scanner.done();

        final var sb = new StringBuilder(in);
        convertTextBlock(processor, sb, in.getPosition(), ranges.getParam());
        in.replace(sb);
        return in.toString();
    }

    @Override
    public void convertTextBlock(Processor processor, final StringBuilder sb, final Position pos, final Params.Param<?>... params) throws BadSyntax {
        assertParams(1, params);
        final var ranges = params[0].asString();

        if (ranges.isPresent()) {
            javax0.jamal.tools.Range.Lines.filter(sb, ranges.get());
        }
    }
}
