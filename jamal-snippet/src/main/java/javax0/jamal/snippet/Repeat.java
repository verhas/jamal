package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
/*
// snippet Repeat_documentation
The macro `repeat` repeats the input string `n` times.
The number of repetitions is given by the parop `n`, also aliased as `times`.


{%sample/
  {@repeat (n=3)A}
%}

will result in

{%output%}

The parop `trim` is optional, and if it is present, then the input string is trimmed before the repetition.

// end snippet
 */
public class Repeat implements Macro, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var n = scanner.number(null, "times", "n");
        final var trim = scanner.bool(null, "trim");
        scanner.done();
        final String string;
        if( trim.is() ){
            string = in.toString().trim();
        }else{
            string = in.toString();
        }
        final var sb = new StringBuilder();
        for( int i = 0 ; i < n.get() ; i++ ){
            sb.append(string);
        }
        return sb.toString();
    }
}
