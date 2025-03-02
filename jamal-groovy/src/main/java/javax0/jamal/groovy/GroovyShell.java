package javax0.jamal.groovy;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

@Macro.Name("groovy:shell")
public class GroovyShell implements Macro, InnerScopeDependent, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var shell = Shell.getShell(in, processor, this);
        final var inputDefinedScriptName = InputHandler.fetch2EOL(in).trim();
        final var scriptName = inputDefinedScriptName.isEmpty() ? "" : inputDefinedScriptName;
        try {
            return "" + shell.evaluate(in.toString(), scriptName);
        } catch (Exception e) {
            throw new BadSyntax("There was an exception '" + e.getMessage() + "' executing the groovy script '" + scriptName + "'.", e);
        }
    }
}
