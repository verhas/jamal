package javax0.jamal.ruby;

import javax0.jamal.api.*;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

@Macro.Name("ruby:import")
public
class RubyImport implements Macro, InnerScopeDependent, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var shell = Shell.getShell(in, processor, this);
        final var scriptName = InputHandler.fetch2EOL(in).trim();
        final Input script;
        if (scriptName.length() > 0) {
            final var fileName = FileTools.absolute(in.getReference(), scriptName);
            script = FileTools.getInput(fileName, processor);
        } else {
            script = in;
        }
        script.append("\n''");
        try {
            shell.evaluate(script.toString(), scriptName);
        } catch (Exception e) {
            throw new BadSyntax("There was an exception '" + e.getMessage() + "' executing the ruby script '" + scriptName + "'.", e);
        }
        return "";
    }

}
