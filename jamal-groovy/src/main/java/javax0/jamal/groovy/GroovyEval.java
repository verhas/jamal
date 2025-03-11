package javax0.jamal.groovy;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

@Macro.Name("groovy:eval")
@Macro.Sentinel("groovy")
public class GroovyEval implements Macro, InnerScopeDependent, Scanner {
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        try {
            final var shell = Shell.getShell(in, processor, this);
            return "" + shell.evaluate(in.toString(), null);
        } catch (Exception e) {
            throw new BadSyntax("Error evaluating groovy script using eval", e);
        }
    }
}
