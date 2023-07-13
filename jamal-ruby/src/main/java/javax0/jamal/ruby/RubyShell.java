package javax0.jamal.ruby;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

public class RubyShell implements Macro, InnerScopeDependent, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var shell = Shell.getShell(in, processor, this);
        final var inputDefinedScriptName = InputHandler.fetch2EOL(in).trim();
        final var scriptName = inputDefinedScriptName.length() == 0 ? "" : inputDefinedScriptName;
        try {
            return "" + shell.evaluate(in.toString(), scriptName);
        } catch (Exception e) {
            throw new BadSyntax("There was an exception '" + e.getMessage() + "' executing the ruby script '" + scriptName + "'.", e);
        }
    }

    @Override
    public String getId() {
        return "ruby:shell";
    }
}
