package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.util.regex.Pattern;

public class RangeMacro implements Macro, InnerScopeDependent, BlockConverter {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var ranges = Params.<Pattern>holder(null, "range", "ranges", "lines").asString();
        Scan.using(processor).from(this).firstLine().keys(ranges).parse(in);

        convertTextBlock(in.getSB(), in.getPosition(), ranges);
        return in.toString();
    }

    @Override
    public void convertTextBlock(final StringBuilder sb, final Position pos, final Params.Param<?>... params) throws BadSyntax {
        assertParams(1, params);
        final var ranges = params[0].asString();

        if (ranges.isPresent()) {
            javax0.jamal.tools.Range.Lines.filter(sb, ranges.get());
        }
    }


    private static final String[] ids = new String[]{"range", "ranges"};

    @Override
    public String[] getIds() {
        return ids;
    }
}
