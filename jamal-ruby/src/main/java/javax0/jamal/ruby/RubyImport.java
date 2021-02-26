package javax0.jamal.ruby;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.InputHandler;

public class RubyImport implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scriptName = InputHandler.fetch2EOL(in).trim();
        final Input script;
        if( scriptName.length() > 0 ) {
            final var fileName = FileTools.absolute(in.getReference(), scriptName);
            script = FileTools.getInput(fileName);
        }else{
            script = in;
        }
        script.getSB().append("\n''");
        final var shell = Shell.getShell(processor);
        try {
            shell.evaluate(script.toString(), scriptName);
        } catch (Exception e) {
            throw new BadSyntax("There was an exception '" + e.getMessage() + "' executing the ruby script '" + scriptName + "'.", e);
        }
        return "";
    }

    @Override
    public String getId() {
        return "ruby:import";
    }
}
