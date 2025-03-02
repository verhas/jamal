package javax0.jamal.ruby;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

@Macro.Name("ruby:eval")
public
class RubyEval implements Macro, InnerScopeDependent, Scanner {
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        try {
            final var shell = Shell.getShell(in, processor, this);
            return "" + shell.evaluate(in.toString(), null);
        } catch (Exception e) {
            throw new BadSyntax("Error evaluating ruby script using eval", e);
        }
    }

}
