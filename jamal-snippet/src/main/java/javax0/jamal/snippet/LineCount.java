package javax0.jamal.snippet;

import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

@Macro.Name("lineCount")
public class LineCount implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) {
        int count = 0;
        for (final var ch : in.toString().toCharArray()) {
            if (ch == '\n') count++;
        }
        return "" + count;
    }
}
