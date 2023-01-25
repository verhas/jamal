package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

/*
snippet numbers

The macro `numbers` can create a comma separated list of numbers.
The numbers can be specified with a start, end and step value.
The default values are `start=0`, and `step=1`.
The end value is mandatory.

The first number is the start value inclusive and the counting ends with the end value exclusive.
You can specify the values with the options keys

* `start`, or `from`
* `end`, or `to`
* `step`, `by`, or `increment`.

Examples:

{%sample/
{@numbers end=5}
{@numbers start=1 end=5}
{@numbers start=1 end=5 step=2}
%}

will result

{%output%}

end snippet
 */
public class Numbers implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var start = Params.<Integer>holder("start", "from").orElseInt(0);
        final var end = Params.<Integer>holder("end", "to").asInt();
        final var step = Params.<Integer>holder("step", "increment", "by").orElseInt(1);
        Scan.using(processor).from(this).tillEnd().keys(start, end, step).parse(in);

        final var startValue = start.get();
        final var endValue = end.get();
        final var stepValue = step.get();

        if (stepValue == 0) {
            throw new BadSyntax(String.format("Step value in '%s' cannot be zero.", getId()));
        }

        final var sb = new StringBuilder();
        var sep = "";

        for (int i = startValue; stepValue > 0 ? i < endValue : i > endValue; i += stepValue) {
            sb.append(sep).append(i);
            sep = ",";
        }
        return sb.toString();
    }
}
